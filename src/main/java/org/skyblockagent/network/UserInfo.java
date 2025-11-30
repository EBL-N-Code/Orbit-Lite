package org.skyblockagent.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class UserInfo {
    public static final Codec<UserInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("plan").forGetter(UserInfo::getPlan),
            Codec.LONG.fieldOf("plan_color").forGetter(UserInfo::getPlanColor),
            Codec.LONG.fieldOf("plan_started_at").forGetter(UserInfo::getPlanStartedAt),
            Codec.LONG.optionalFieldOf("plan_duration", -1L).forGetter(UserInfo::getPlanDuration),
            Codec.STRING.optionalFieldOf("next_plan", "").forGetter(UserInfo::getNextPlan),
            Codec.LONG.optionalFieldOf("next_plan_color", -1L).forGetter(UserInfo::getNextPlanColor),
            Codec.INT.fieldOf("daily_tokens").forGetter(UserInfo::getDailyTokens),
            Codec.INT.fieldOf("tokens_used_today").forGetter(UserInfo::getTokensUsedToday)
    ).apply(instance, UserInfo::new));

    private final String plan;
    private final long planColor;
    private final long planStartedAt;
    private final Long planDuration;
    private final String nextPlan;
    private final Long nextPlanColor;
    private final int dailyTokens;
    private int tokensUsedToday;

    public UserInfo(String plan, long planColor, long planStartedAt, long planDuration, String nextPlan, Long nextPlanColor, int dailyTokens, int tokensUsedToday) {
        this.plan = plan;
        this.planColor = planColor;
        this.planStartedAt = planStartedAt;
        this.planDuration = planDuration > 0 ? planDuration : null;
        this.nextPlan = !nextPlan.isEmpty() ? nextPlan : null;
        this.nextPlanColor = nextPlanColor > 0 ? nextPlanColor : null;
        this.dailyTokens = dailyTokens;
        this.tokensUsedToday = tokensUsedToday;
    }

    public String getPlan() {
        return plan;
    }

    public long getPlanColor() {
        return planColor;
    }

    public long getPlanStartedAt() {
        return planStartedAt;
    }

    public @Nullable Long getPlanDuration() {
        return planDuration;
    }

    public @Nullable String getNextPlan() {
        return nextPlan;
    }

    public @Nullable Long getNextPlanColor() {
        return nextPlanColor;
    }

    public int getDailyTokens() {
        return dailyTokens;
    }

    public int getTokensUsedToday() {
        return tokensUsedToday;
    }

    public void addTokens(int tokens) {
        this.tokensUsedToday += tokens;
    }
}
