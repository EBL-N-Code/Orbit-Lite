package org.skyblockagent.gui;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.cursor.StandardCursors;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.skyblockagent.Skyblockagent;
import org.skyblockagent.chat.ChatManager;
import org.skyblockagent.gui.components.Widget;
import org.skyblockagent.gui.components.dropdown.Dropdown;
import org.skyblockagent.network.ApiAuth;
import org.skyblockagent.network.UserInfo;
import org.skyblockagent.render.Renderer;
import org.skyblockagent.utils.TextUtils;

import java.util.Optional;

public class Header extends Widget {
    private static final int PROFILE_BUTTON_SIZE = 18;
    private String title;
    private int titleX;
    private boolean profileButtonHovered;
    private Dropdown profileDropdown;
    private int dropdownX, dropdownY;

    @Override
    public void init() {
        title = ChatManager.getInstance().getChat().getName();
        if (title != null) {
            ChatScreen chatScreen = ChatScreen.getInstance();
            titleX = chatScreen.getLeft() + (chatScreen.getChatWidth() / 2);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderProfileButton(context, mouseX, mouseY);
        if (title != null) Renderer.drawCenteredText(context, title, titleX, y + 9);
        if (profileDropdown != null) profileDropdown.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderProfileButton(DrawContext context, int mouseX, int mouseY) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        int size = PROFILE_BUTTON_SIZE;
        int x = this.x + width - size - 8;
        int y = this.y + (height / 2) - (size / 2) + 2;

        PlayerSkinDrawer.draw(context, player.getSkin(), x, y, size);

        UserInfo userInfo = ApiAuth.getUserInfo();
        if (userInfo != null) {
            int borderColor = userInfo.getPlanColor();
            Renderer.drawRect(context, x, y, size, size, 0, 0, borderColor, 1f);
        }

        profileButtonHovered = mouseX >= x
                && mouseX <= x + size
                && mouseY >= y
                && mouseY <= y + size;

        if (profileButtonHovered) {
            context.setCursor(StandardCursors.POINTING_HAND);
            Renderer.drawRect(context, x, y, size, size, 0, 0x33FFFFFF);
            dropdownX = x + (size / 2);
            dropdownY = y + size + 2;
        }
    }

    public boolean dropdownClicked(double mouseX, double mouseY) {
        if (profileDropdown == null) return false;
        if (profileButtonHovered) {
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
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (profileButtonHovered) {
            createProfileDropdown();
            return true;
        }
        return false;
    }

    public void createProfileDropdown() {
        Dropdown.Builder builder = Dropdown.builder("Profile")
                .position(dropdownX, dropdownY, true);

        UserInfo userInfo = ApiAuth.getUserInfo();
        if (userInfo == null) {
            builder.addText(Text.literal("The mod couldn't import").formatted(Formatting.RED))
                    .addText(Text.literal("your data.").formatted(Formatting.RED))
                    .addText(Text.literal("Please report this!").formatted(Formatting.RED));
        } else {
            String planName = userInfo.getPlanName();
            int planColor = userInfo.getPlanColor();
            long planOwnedAt = userInfo.getPlanOwnedAt();
            Optional<Long> planDuration = Optional.ofNullable(userInfo.getPlanDuration());
            String tokensUsed = TextUtils.formatNumber(userInfo.getUsedTokensToday());
            String maxTokens = TextUtils.formatNumber(userInfo.getDailyMaxTokens());

            MutableText planText = Text.literal("★ Plan: ").formatted(Formatting.WHITE)
                    .append(Text.literal(planName).styled(style -> style.withColor(planColor)));

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

            float tokenPercentage = (float) userInfo.getUsedTokensToday() / userInfo.getDailyMaxTokens();
            Formatting tokenColor = tokenPercentage > 0.9f ?
                    Formatting.RED : tokenPercentage > 0.7f ?
                    Formatting.YELLOW : Formatting.GREEN;
            Text tokensText = Text.literal("⚡ Tokens: ")
                    .formatted(Formatting.WHITE)
                    .append(Text.literal(tokensUsed + "/" + maxTokens).formatted(tokenColor))
                    .append(Text.literal(String.format(" (%.0f%%)", tokenPercentage * 100)).formatted(Formatting.GRAY));
            builder.addText(planText).addText(tokensText);
        }

        ContactInformation contact = FabricLoader.getInstance()
                .getModContainer(Skyblockagent.NAMESPACE)
                .orElseThrow()
                .getMetadata()
                .getContact();

        Screen screen = ChatScreen.getInstance();
        profileDropdown = builder
                .addSeparator()
                .addOption("Report a Bug", () ->
                        contact.get("issues").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Help/FAQ", () ->
                        contact.get("homepage").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Changelog/Releases", () ->
                        contact.get("sources").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("Join Discord Server", () ->
                        contact.get("discord").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .addOption("GitHub Repository", () ->
                        contact.get("sources").ifPresent(url -> ConfirmLinkScreen.open(screen, url)))
                .build();
    }
}
