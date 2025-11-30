package org.skyblockagent.gui.header;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
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
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.chat.ChatMode;
import org.skyblockagent.config.ConfigManager;
import org.skyblockagent.gui.ChatScreen;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.gui.components.dropdown.Dropdown;
import org.skyblockagent.network.ApiAuth;
import org.skyblockagent.network.UserInfo;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.utils.TextUtils;

import java.util.Optional;

public class Header extends Widget {
    private static final int PROFILE_BUTTON_SIZE = 18;
    private static SkinTextures PlayerHeadSkin;
    private String title;
    private int titleX;

    private Dropdown profileDropdown;
    private int profileDropdownX, profileDropdownY;
    private boolean profileDropdownHovered;

    private Dropdown chatModeDropdown;
    private int chatModeDropdownX, chatModeDropdownY;
    private boolean chatModeDropdownHovered;

    @Override
    public void init() {
        title = ChatManager.getInstance().getChat().getName();
        if (title != null) {
            ChatScreen chatScreen = ChatScreen.getInstance();
            titleX = chatScreen.getLeft() + (chatScreen.getChatWidth() / 2);
        }

        if (PlayerHeadSkin == null) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            if (player != null && networkHandler != null) {
                PlayerListEntry playerListEntry = networkHandler.getPlayerListEntry(player.getUuid());
                if (playerListEntry != null) {
                    PlayerHeadSkin = playerListEntry.getSkinTextures();
                }
            }
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderChatModeButton(context, mouseX, mouseY);
        if (chatModeDropdown != null) chatModeDropdown.render(context, mouseX, mouseY, deltaTicks);

        if (title != null) Renderer.drawCenteredText(context, title, titleX, y + (height / 2) - 3);

        renderProfileButton(context, mouseX, mouseY);
        if (profileDropdown != null) profileDropdown.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderChatModeButton(DrawContext context, int mouseX, int mouseY) {
        int x = this.x + 5;
        int y = this.y + (height / 2) - 6;

        String chatMode = ConfigManager.getState().getChatMode().getName();
        int chatModeWidth = this.textRenderer.getWidth(chatMode);
        Renderer.drawText(context, chatMode, x + 5, y + 4);

        MutableText arrowHead = Text.literal("⌄").withColor(0xFF7a7f8a);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x + chatModeWidth + 8, y - 4);
        context.getMatrices().scale(2f, 2f);
        Renderer.drawText(context, arrowHead, 0, 0);
        context.getMatrices().popMatrix();

        int width = chatModeWidth + 17;
        int height = 16;

        chatModeDropdownHovered = mouseX >= x
                && mouseX <= x + width
                && mouseY >= y
                && mouseY <= y + height;

        if (chatModeDropdownHovered) {
            context.setCursor(StandardCursors.POINTING_HAND);
            Renderer.drawRect(context, x, y, width, height, 4f, 0x20FFFFFF);
            chatModeDropdownX = x;
            chatModeDropdownY = y + height + 2;
        }
    }

    private void renderProfileButton(DrawContext context, int mouseX, int mouseY) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        int size = PROFILE_BUTTON_SIZE;
        int x = this.x + width - size - 8;
        int y = this.y + (height / 2) - (size / 2) + 2;

        SkinTextures skin = PlayerHeadSkin != null ? PlayerHeadSkin : player.getSkin();
        PlayerSkinDrawer.draw(context, skin, x, y, size);

        UserInfo userInfo = ApiAuth.getUserInfo();
        if (userInfo != null) {
            int borderColor = (int) userInfo.getPlanColor();
            Renderer.drawRect(context, x, y, size, size, 0, 0, borderColor, 1f);
        }

        profileDropdownHovered = mouseX >= x
                && mouseX <= x + size
                && mouseY >= y
                && mouseY <= y + size;

        if (profileDropdownHovered) {
            context.setCursor(StandardCursors.POINTING_HAND);
            Renderer.drawRect(context, x, y, size, size, 0, 0x33FFFFFF);
            profileDropdownX = x + (size / 2);
            profileDropdownY = y + size + 2;
        }
    }

    public boolean dropdownClicked(double mouseX, double mouseY) {
        if (profileDropdown != null) {
            if (profileDropdownHovered) {
                profileDropdown = null;
                return true;
            }
            if (profileDropdown.isHovered(mouseX, mouseY)) {
                if (profileDropdown.handleClick()) {
                    profileDropdown = null;
                }
                return true;
            }
            profileDropdown = null;
        } else if (chatModeDropdown != null) {
            if (chatModeDropdownHovered) {
                chatModeDropdown = null;
                return true;
            }
            if (chatModeDropdown.isHovered(mouseX, mouseY)) {
                if (chatModeDropdown.handleClick()) {
                    chatModeDropdown = null;
                }
                return true;
            }
            chatModeDropdown = null;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (profileDropdownHovered) {
            createProfileDropdown();
            return true;
        } else if (chatModeDropdownHovered) {
            createChatModeDropdown();
            return true;
        }
        return false;
    }

    public void createChatModeDropdown() {
        Dropdown.Builder builder = Dropdown.builder("ChatMode").position(chatModeDropdownX, chatModeDropdownY, false);
        for (ChatMode mode : ChatMode.values()) {
            builder.addOption(mode.getName() + " Mode", () -> {
                ConfigManager.getState().setChatMode(mode);
                ConfigManager.save();
            });
        }

        chatModeDropdown = builder.build();
    }

    public void createProfileDropdown() {
        Dropdown.Builder builder = Dropdown.builder("Profile").position(profileDropdownX, profileDropdownY, true);

        UserInfo userInfo = ApiAuth.getUserInfo();
        if (userInfo == null) {
            builder.addText(Text.literal("The mod couldn't import").formatted(Formatting.RED))
                    .addText(Text.literal("your data.").formatted(Formatting.RED))
                    .addText(Text.literal("Please report this!").formatted(Formatting.RED));
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
                .getModContainer(Skyblockagent.NAMESPACE)
                .orElseThrow()
                .getMetadata()
                .getContact();

        Screen screen = ChatScreen.getInstance();
        profileDropdown = builder
                .addSeparator()
                .addOption("Report a Bug", () -> contact.get("issues").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Help/FAQ", () -> contact.get("homepage").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Changelog/Releases", () -> contact.get("sources").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Join Discord Server", () -> contact.get("discord").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("GitHub Repository", () -> contact.get("sources").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .build();
    }
}
