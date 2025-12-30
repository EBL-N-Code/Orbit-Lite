package org.apex.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import org.apex.Apex;
import org.jetbrains.annotations.Nullable;
import org.apex.Constants;
import org.apex.config.ModState;
import org.apex.mixins.accessors.MinecraftClientAccessor;
import org.apex.utils.CommandManager;
import org.apex.utils.Logger;
import org.apex.utils.Scheduler;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import static org.apex.Apex.GSON;

public class ApiAuth {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final int TOKEN_REFRESH_INTERVAL_TICKS = 5 * 60 * 20;
    private static int taskId = -1;

    private static String token;
    private static UserInfo userInfo;

    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(ignored -> {
            if (taskId == -1) {
                taskId = Scheduler.getInstance().scheduleCyclic(ApiAuth::updateToken, 0, TOKEN_REFRESH_INTERVAL_TICKS);
            } else {
                Scheduler.getInstance().resetTask(taskId);
            }
        });
        CommandManager.register("refresh", () -> {
            Logger.sendToPlayer(Text.literal("Refreshing API Token...").formatted(Formatting.WHITE));
            Scheduler.getInstance().resetTask(taskId);
            updateToken();
        }, 3000, true);
    }

    private static void updateToken() {
        ProfileKeys profileKeys = ((MinecraftClientAccessor) CLIENT).getProfileKeys();
        profileKeys.fetchKeyPair().thenAcceptAsync(playerKeypairOpt -> {
            boolean expired = playerKeypairOpt.map(keyPair -> keyPair.publicKey().data().isExpired()).orElse(false);
            if (playerKeypairOpt.isEmpty() || expired) {
                Logger.error("Failed to refresh the API token!", new IllegalStateException("Profile keys missing or expired"), true);
                return;
            }

            TokenRequest tokenRequest = Objects.requireNonNull(createTokenRequest(playerKeypairOpt.get()));
            String request = GSON.toJson(TokenRequest.CODEC.encodeStart(JsonOps.INSTANCE, tokenRequest).getOrThrow());

            try {
                HttpUtils.sendPostRequest(Constants.SERVER_IP + "/auth", request, null)
                        .thenAccept(response -> CLIENT.execute(() -> {
                            try {
                                if (response == null) throw new NullPointerException("Failed to get a response");
                                int statusCode = response.statusCode();
                                String body = response.body();
                                JsonObject element = JsonParser.parseString(body).getAsJsonObject();

                                if (statusCode != 200) {
                                    String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                    throw new RuntimeException("API returned " + statusCode + " code: " + error);
                                }

                                token = element.get("token").getAsString();
                                userInfo = UserInfo.CODEC.parse(JsonOps.INSTANCE, element.get("user_info")).getOrThrow();

                                JsonElement maintenanceElement = element.get("maintenance");
                                boolean maintenance = maintenanceElement != null && maintenanceElement.getAsBoolean();

                                ModState.setUnderMaintenance(maintenance);
                                if (maintenance) ModState.disableMod();

                                if (ModState.isDisabled() && !maintenance) {
                                    ModState.enableMod();
                                }

                                JsonElement notification = element.get("notification");
                                if (notification != null) {
                                    Logger.sendToPlayer(Text.of(notification.getAsString()));
                                }

                                BackendService.fetchChats();
                            } catch (Exception e) {
                                Logger.error("Failed to refresh the API token!", e, true);
                            }
                        }));
            } catch (Exception e) {
                Logger.error("Failed to refresh the API token!", e, true);
            }
        }).exceptionally(e -> {
            Logger.error("Failed to refresh the API token!", e, true);
            return null;
        });
    }

    private static TokenRequest createTokenRequest(PlayerKeyPair playerKeyPair) {
        try {
            UUID sessionUUID = Objects.requireNonNull(CLIENT.getSession().getUuidOrNull());
            PlayerPublicKey.PublicKeyData publicKeyData = playerKeyPair.publicKey().data();
            String publicKey = Base64.getEncoder().encodeToString(publicKeyData.key().getEncoded());
            byte[] publicKeySignature = publicKeyData.keySignature();
            long expiresAt = publicKeyData.expiresAt().toEpochMilli();

            TokenRequest.KeyPairInfo keyPairInfo = new TokenRequest.KeyPairInfo(sessionUUID, publicKey, publicKeySignature, expiresAt);
            TokenRequest.SignedData signedData = Objects.requireNonNull(getRandomSignedData(playerKeyPair.privateKey()));

            return new TokenRequest(keyPairInfo, signedData, Apex.MINECRAFT_VERSION, Apex.VERSION);
        } catch (Exception e) {
            Logger.error("Failed to create Token Request!", e);
            return null;
        }
    }

    private static TokenRequest.SignedData getRandomSignedData(PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            UUID uuid = UUID.randomUUID();
            ByteBuffer buf = ByteBuffer.allocate(16)
                    .putLong(uuid.getMostSignificantBits())
                    .putLong(uuid.getLeastSignificantBits());
            signature.initSign(privateKey);
            signature.update(buf.array());
            return new TokenRequest.SignedData(buf.array(), signature.sign());
        } catch (Exception e) {
            Logger.error("Failed to sign random data!", e);
            return null;
        }
    }

    public static @Nullable String getToken() {
        return token;
    }

    public static @Nullable UserInfo getUserInfo() {
        return userInfo;
    }

    private record TokenRequest(KeyPairInfo keyPairInfo, SignedData signedData, String minecraftVersion, String modVersion) {
        private static final Codec<TokenRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        KeyPairInfo.CODEC.fieldOf("key_pair").forGetter(TokenRequest::keyPairInfo),
                        SignedData.CODEC.fieldOf("signed_data").forGetter(TokenRequest::signedData),
                        Codec.STRING.fieldOf("minecraft_version").forGetter(TokenRequest::minecraftVersion),
                        Codec.STRING.fieldOf("mod_version").forGetter(TokenRequest::modVersion))
                .apply(instance, TokenRequest::new));

        private record KeyPairInfo(UUID uuid, String publicKey, byte[] publicKeySignature, long expiresAt) {
            private static final Codec<KeyPairInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                            Uuids.STRING_CODEC.fieldOf("uuid").forGetter(KeyPairInfo::uuid),
                            Codec.STRING.fieldOf("public_key").forGetter(KeyPairInfo::publicKey),
                            Codecs.BASE_64.fieldOf("public_key_signature").forGetter(KeyPairInfo::publicKeySignature),
                            Codec.LONG.fieldOf("expires_at").forGetter(KeyPairInfo::expiresAt))
                    .apply(instance, KeyPairInfo::new));
        }

        private record SignedData(byte[] original, byte[] signed) {
            private static final Codec<SignedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                            Codecs.BASE_64.fieldOf("original").forGetter(SignedData::original),
                            Codecs.BASE_64.fieldOf("signed").forGetter(SignedData::signed))
                    .apply(instance, SignedData::new));
        }
    }
}