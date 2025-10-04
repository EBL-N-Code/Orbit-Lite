package org.skyblockagent.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.mixins.accessors.MinecraftClientAccessor;
import org.skyblockagent.utils.CommandManager;
import org.skyblockagent.utils.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.*;

public class ApiAuth {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAuth.class);
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final String AUTH_URL = "http://localhost:3000/auth";
    private static final String ALGORITHM = "SHA256withRSA";
    private static final int TOKEN_REFRESH_INTERVAL_TICKS = 300 * 20;
    private static final Gson GSON = new Gson();

    private static int taskId = -1;
    private static boolean isModActive;
    private static boolean sentErrorOnce;
    private static boolean executedByCmd;
    private static Text statusMessage;

    private static String token;
    private static UserInfo userInfo;

    public static void init() {
        ClientLifecycleEvents.CLIENT_STARTED.register(_ -> {
            switch (taskId == -1) {
                case true -> taskId = Scheduler.getInstance().scheduleCyclic(ApiAuth::updateToken, 0, TOKEN_REFRESH_INTERVAL_TICKS);
                case false -> Scheduler.getInstance().resetTask(taskId);
            }
        });
        ClientPlayConnectionEvents.JOIN.register((_, _, client) -> {
            if (statusMessage != null && client.player != null) {
                client.player.sendMessage(statusMessage, false);
                statusMessage = null;
            }
        });
        CommandManager.register("updateToken", () -> {
            Scheduler.getInstance().resetTask(taskId);
            executedByCmd = true;
            updateToken();
        }, 3000);
    }

    private static void updateToken() {
        ProfileKeys profileKeys = ((MinecraftClientAccessor) CLIENT).getProfileKeys();
        profileKeys.fetchKeyPair().thenAcceptAsync(playerKeypairOpt -> {
            boolean expired = playerKeypairOpt.map(keyPair -> keyPair.publicKey().data().isExpired()).orElse(false);
            if (playerKeypairOpt.isPresent() && !expired) {
                TokenRequest tokenRequest = createTokenRequest(playerKeypairOpt.get());
                if (tokenRequest == null) return;

                String request = GSON.toJson(TokenRequest.CODEC.encodeStart(JsonOps.INSTANCE, tokenRequest).getOrThrow());

                try {
                    HttpUtils.sendPostRequest(AUTH_URL, request, null)
                            .thenAccept(response -> CLIENT.execute(() -> {
                                try {
                                    if (response == null) return;
                                    int statusCode = response.statusCode();
                                    String body = response.body();
                                    JsonObject element = JsonParser.parseString(body).getAsJsonObject();

                                    if (statusCode != 200) {
                                        String error = element.has("error") ? element.get("error").getAsString() : "Unknown error";
                                        logErr("[SkyBlockAI] API Request returned " + statusCode + ": " + error);
                                        return;
                                    }

                                    token = element.get("token").getAsString();
                                    userInfo = UserInfo.CODEC.parse(JsonOps.INSTANCE, element.get("user_info")).getOrThrow();

                                    if (executedByCmd || !isModActive) {
                                        sendMsgToPlayer(Text.literal(
                                                "[SkyBlockAI] Successfully Imported Your Data\n" +
                                                        "Your plan is " + userInfo.plan() + "\n" +
                                                        "Tokens: " + userInfo.usedTokensToday() + "/" + userInfo.dailyMaxTokens() + "\n" +
                                                        "You got that plan in " + userInfo.planOwnedAt() + "\n" +
                                                        "Your plan will expire in " + userInfo.planDuration()
                                        ).formatted(Formatting.GREEN));
                                    }
                                    executedByCmd = false;
                                    sentErrorOnce = false;
                                    isModActive = true;
                                    if (!BackendService.hasFetchedChats()) BackendService.fetchChats();
                                } catch (Exception e) {
                                    logErr("[SkyBlockAI] Failed to refresh the api token!", e);
                                }
                            }));
                } catch (Exception e) {
                    logErr("[SkyBlockAI] Failed to refresh the api token!", e);
                }
            } else {
                logErr("[SkyBlockAI] Failed to fetch profile keys!");
            }
        }).exceptionally(throwable -> {
            logErr("[SkyBlockAI] Encountered an unexpected exception while refreshing the api token!", throwable);
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
            TokenRequest.SignedData signedData = getRandomSignedData(playerKeyPair.privateKey());
            if (signedData == null) return null;
            return new TokenRequest(keyPairInfo, signedData, Skyblockagent.MINECRAFT_VERSION, Skyblockagent.VERSION);
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to create Token Request!", e);
            return null;
        }
    }

    private static TokenRequest.SignedData getRandomSignedData(PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            UUID uuid = UUID.randomUUID();
            ByteBuffer buf = ByteBuffer.allocate(16)
                    .putLong(uuid.getMostSignificantBits())
                    .putLong(uuid.getLeastSignificantBits());
            signature.initSign(privateKey);
            signature.update(buf.array());
            return new TokenRequest.SignedData(buf.array(), signature.sign());
        } catch (Exception e) {
            logErr("[SkyBlockAI] Failed to sign random data!", e);
            return null;
        }
    }

    //TODO: move to error handling class
    public static void logErr(String message, Object... logArgs) {
        LOGGER.error(message, logArgs);
        Text text = Text.literal(message).formatted(Formatting.RED);

        if (executedByCmd || isModActive || !sentErrorOnce) sendMsgToPlayer(text);
        executedByCmd = false;
        sentErrorOnce = true;
        isModActive = false;
    }

    private static void sendMsgToPlayer(Text message) {
        if (CLIENT.player != null) {
            CLIENT.player.sendMessage(message, false);
            return;
        }
        statusMessage = message;
    }

    @Nullable
    public static String getToken() {
        return token;
    }

    @Nullable
    public static UserInfo getUserInfo() {
        return userInfo;
    }

    public static boolean isModActive() {
        return isModActive;
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

    public record UserInfo(String plan, long planOwnedAt, Optional<Long> planDuration, int dailyMaxTokens, int usedTokensToday) {
        public static final Codec<UserInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("plan").forGetter(UserInfo::plan),
                Codec.LONG.fieldOf("plan_owned_at").forGetter(UserInfo::planOwnedAt),
                Codec.LONG.optionalFieldOf("plan_duration").forGetter(UserInfo::planDuration),
                Codec.INT.fieldOf("daily_max_tokens").forGetter(UserInfo::dailyMaxTokens),
                Codec.INT.fieldOf("used_tokens_today").forGetter(UserInfo::usedTokensToday)
        ).apply(instance, UserInfo::new));
    }
}