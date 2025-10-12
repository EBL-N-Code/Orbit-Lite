package org.skyblockagent.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class UserInfo {
    public static final Codec<UserInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("plan").forGetter(UserInfo::getPlan),
            Codec.LONG.fieldOf("plan_owned_at").forGetter(UserInfo::getPlanOwnedAt),
            Codec.LONG.optionalFieldOf("plan_duration", null).forGetter(UserInfo::getPlanDuration),
            Codec.INT.fieldOf("daily_max_tokens").forGetter(UserInfo::getDailyMaxTokens),
            Codec.INT.fieldOf("used_tokens_today").forGetter(UserInfo::getUsedTokensToday)
    ).apply(instance, UserInfo::new));

    private final String plan;
    private final long planOwnedAt;
    private final Long planDuration;
    private final int dailyMaxTokens;
    private int usedTokensToday;

    public UserInfo(String plan, long planOwnedAt, Long planDuration, int dailyMaxTokens, int usedTokensToday) {
        this.plan = plan;
        this.planOwnedAt = planOwnedAt;
        this.planDuration = planDuration;
        this.dailyMaxTokens = dailyMaxTokens;
        this.usedTokensToday = usedTokensToday;
    }

    public String getPlanName() {
        return plan.substring(0, 1).toUpperCase() + plan.substring(1).toLowerCase();
    }

    public int getPlanColor() {
        return switch (plan) {
            case "PLUS" -> new Color(80, 200, 255).getRGB();
            case "PRO" -> new Color(120, 50, 220).getRGB();
            default -> new Color(170, 170, 170).getRGB();
        };
    }

    public String getPlan() {
        return plan;
    }

    public long getPlanOwnedAt() {
        return planOwnedAt;
    }

    @Nullable
    public Long getPlanDuration() {
        return planDuration;
    }

    public int getDailyMaxTokens() {
        return dailyMaxTokens;
    }

    public int getUsedTokensToday() {
        return usedTokensToday;
    }

    public void setUsedTokensToday(int usedTokensToday) {
        this.usedTokensToday = usedTokensToday;
    }
}
