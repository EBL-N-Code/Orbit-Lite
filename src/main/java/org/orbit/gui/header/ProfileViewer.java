package org.orbit.gui.header;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.orbit.Constants;
import org.orbit.Orbit;
import org.orbit.gui.ChatScreen;
import org.orbit.gui.components.Selector;
import org.orbit.gui.components.dropdown.ClickableOption;
import org.orbit.gui.components.dropdown.Dropdown;
import org.orbit.network.ApiAuth;
import org.orbit.network.HttpUtils;
import org.orbit.network.UserInfo;
import org.orbit.render.Renderer;
import org.orbit.utils.ColorUtils;
import org.orbit.utils.Logger;
import org.orbit.utils.TextUtils;

import java.util.Optional;

public class ProfileViewer extends Selector {
    public static final int SIZE = 18;
    private static SkinTextures PlayerHeadTexture;

    public ProfileViewer(int x, int y) {
        super(x, y);
        fetchPlayerHeadTexture();
    }

    private void fetchPlayerHeadTexture() {
        if (PlayerHeadTexture == null) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            if (player != null && networkHandler != null) {
                PlayerListEntry playerListEntry = networkHandler.getPlayerListEntry(player.getUuid());
                if (playerListEntry != null) {
                    PlayerHeadTexture = playerListEntry.getSkinTextures();
                }
            }
        }
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        SkinTextures skin = PlayerHeadTexture != null ? PlayerHeadTexture : player.getSkin();
        PlayerSkinDrawer.draw(context, skin, x, y, SIZE);

        UserInfo userInfo = ApiAuth.getUserInfo();
        if (userInfo != null) {
            int borderColor = (int) userInfo.getPlanColor();
            Renderer.drawRect(context, x, y, SIZE, SIZE, 0, 0, borderColor, 1f);
        }

        hovered = mouseX >= x
                && mouseX <= x + SIZE
                && mouseY >= y
                && mouseY <= y + SIZE;

        if (hovered) {
            context.setCursor(StandardCursors.POINTING_HAND);
            Renderer.drawRect(context, x, y, SIZE, SIZE, 0, 0x33FFFFFF);
        }
    }

    @Override
    public void createDropdown() {
        Dropdown.Builder builder = Dropdown.builder("Profile").position(x + (SIZE / 2), y + SIZE + 2, true);

        UserInfo userInfo = ApiAuth.getUserInfo();
        if (userInfo == null) {
            builder.addText(Constants.IMPORT_FAILED);
            builder.addText(Constants.REPORT_ISSUE);
        } else {
            String plan = userInfo.getPlan();
            int planColor = (int) userInfo.getPlanColor();
            long planOwnedAt = userInfo.getPlanStartedAt();
            Optional<Long> planDuration = Optional.ofNullable(userInfo.getPlanDuration());
            String tokensUsed = TextUtils.formatNumber(userInfo.getTokensUsedToday());
            String maxTokens = TextUtils.formatNumber(userInfo.getDailyTokens());

            MutableText planText = Text.literal("★ Plan: ").formatted(Formatting.WHITE)
                    .append(Text.literal(plan).styled(style -> style.withColor(planColor)));

            planDuration.ifPresent(duration -> {
                long expiresAt = planOwnedAt + duration;
                long currentTime = System.currentTimeMillis() / 1000;
                long timeLeft = expiresAt - currentTime;

                if (timeLeft > 0) {
                    long days = timeLeft / (60 * 60 * 24);
                    long hours = (timeLeft / (60 * 60)) % 24;

                    String timeLeftText = days > 0 ?
                            String.format(" (%dd %dh left)", days, hours) :
                            String.format(" (%dh %dm left)", hours, (timeLeft / 60) % 60);

                    Formatting timeColor = days < 1 ?
                            Formatting.RED : days < 3 ?
                            Formatting.YELLOW : Formatting.GRAY;
                    planText.append(Text.literal(timeLeftText).formatted(timeColor));
                }
            });

            float tokenPercentage = (float) userInfo.getTokensUsedToday() / userInfo.getDailyTokens();
            Formatting tokenColor = tokenPercentage > 0.9f ?
                    Formatting.RED : tokenPercentage > 0.7f ?
                    Formatting.YELLOW : Formatting.GREEN;
            Text tokensText = Text.literal("⚡ Tokens: ")
                    .formatted(Formatting.WHITE)
                    .append(Text.literal(tokensUsed + "/" + maxTokens).formatted(tokenColor))
                    .append(Text.literal(String.format(" (%.0f%%)", tokenPercentage * 100)).formatted(Formatting.GRAY));

            builder.addText(planText)
                    .addText(tokensText);

            String nextPlan = userInfo.getNextPlan();
            Long nextPlanColor = userInfo.getNextPlanColor();
            if (nextPlan != null && nextPlanColor != null) {
                builder.addClickableOption(new UpgradeOption("Upgrade to " + nextPlan, nextPlanColor.intValue()));
            }
        }

        ContactInformation contact = FabricLoader.getInstance()
                .getModContainer(Orbit.NAMESPACE)
                .orElseThrow()
                .getMetadata()
                .getContact();

        Screen screen = ChatScreen.getInstance();
        dropdown = builder
                .addSeparator()
                .addOption("Report a Bug", () -> contact.get("issues").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Website", () -> contact.get("homepage").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Releases", () -> contact.get("modrinth").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Github", () -> contact.get("issues").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .build();
    }

    public static class UpgradeOption extends ClickableOption {
        private final static int HEIGHT = 17;
        private final int color;

        public UpgradeOption(String label, int color) {
            super(label, UpgradeOption::click);
            this.color = color;
        }

        private static void click() {
            ConfirmLinkScreen.open(ChatScreen.getInstance(), Constants.UPGRADE_URL);
            String username = MinecraftClient.getInstance().getSession().getUsername();

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("username", username);
            String request = requestBody.toString();

            try {
                HttpUtils.sendPostRequest(Constants.SERVER_IP + "/interest", request, null);
            } catch (Exception e) {
                Logger.error("Failed to send interest!", e);
            }
        }

        @Override
        public void render(DrawContext context, int x, int y, int width, boolean isHovered, TextRenderer textRenderer) {
            width -= 8;
            x += 4;

            int colorWithOpacity = ColorUtils.applyOpacity(color, 60);
            Renderer.drawRect(context, x, y, width, HEIGHT, 4, colorWithOpacity, color, 1f);
            Renderer.drawCenteredText(context, label, x + (width / 2), y + (HEIGHT / 2) - 3, 0xFFFFFFFF);

            if (isHovered) {
                Renderer.drawRect(context, x, y, width, HEIGHT, 4, 0x15FFFFFF);
            }
        }

        @Override
        public int getHeight() {
            return HEIGHT;
        }
    }
}
