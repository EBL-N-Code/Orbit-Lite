package org.apex.chat;

import java.util.*;

public class ChatGrouper {

    public static Map<DateGroup, List<ChatSummary>> groupChats(List<ChatSummary> chats) {
        Map<DateGroup, Long> thresholds = computeThresholds(System.currentTimeMillis());
        Map<DateGroup, List<ChatSummary>> groups = new EnumMap<>(DateGroup.class);

        // Initialize groups
        for (DateGroup group : DateGroup.values()) {
            groups.put(group, new ArrayList<>());
        }

        // Group chats
        for (ChatSummary chat : chats) {
            DateGroup group = classify(chat.updatedAt(), thresholds);
            groups.get(group).add(chat);
        }

        // Sort inside each group
        for (List<ChatSummary> chatList : groups.values()) {
            chatList.sort(Comparator.comparing(ChatSummary::updatedAt).reversed());
        }

        return groups;
    }

    private static DateGroup classify(Long timestampSeconds, Map<DateGroup, Long> thresholds) {
        if (timestampSeconds == null) return DateGroup.OLDER;
        long tsMillis = timestampSeconds * 1000;
        if (tsMillis >= thresholds.get(DateGroup.TODAY)) return DateGroup.TODAY;
        if (tsMillis >= thresholds.get(DateGroup.YESTERDAY)) return DateGroup.YESTERDAY;
        if (tsMillis >= thresholds.get(DateGroup.THIS_WEEK)) return DateGroup.THIS_WEEK;
        if (tsMillis >= thresholds.get(DateGroup.THIS_MONTH)) return DateGroup.THIS_MONTH;
        return DateGroup.OLDER;
    }

    private static Map<DateGroup, Long> computeThresholds(long nowMillis) {
        Map<DateGroup, Long> thresholds = new EnumMap<>(DateGroup.class);
        Calendar cal = Calendar.getInstance();

        // TODAY
        cal.setTimeInMillis(nowMillis);
        resetToStartOfDay(cal);
        thresholds.put(DateGroup.TODAY, cal.getTimeInMillis());

        // YESTERDAY
        Calendar yesterday = (Calendar) cal.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        thresholds.put(DateGroup.YESTERDAY, yesterday.getTimeInMillis());

        // THIS_WEEK
        Calendar week = (Calendar) cal.clone();
        week.set(Calendar.DAY_OF_WEEK, week.getFirstDayOfWeek());
        resetToStartOfDay(week);
        thresholds.put(DateGroup.THIS_WEEK, week.getTimeInMillis());

        // THIS_MONTH
        Calendar month = (Calendar) cal.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        resetToStartOfDay(month);
        thresholds.put(DateGroup.THIS_MONTH, month.getTimeInMillis());

        return thresholds;
    }

    private static void resetToStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
