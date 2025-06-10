package org.skyblockagent.chat;

import net.minecraft.util.Pair;

import java.util.*;

public class ChatGrouper {
    private static final Map<DateGroup, Long> GROUP_THRESHOLDS = computeThresholds(System.currentTimeMillis());

    public static Pair<Integer, Map<DateGroup, List<Chat>>> groupChats(List<Chat> chats) {
        Map<DateGroup, List<Chat>> groupedChats = new EnumMap<>(DateGroup.class);
        int maxOffset = 0;

        // Initialize lists for each group
        for (DateGroup group : DateGroup.values()) {
            groupedChats.put(group, new ArrayList<>());
        }

        // Group chats
        for (Chat chat : chats) {
            DateGroup group = classify(chat.getModifiedAt());
            groupedChats.get(group).add(chat);
        }

        // Sort chats within each group by lastModified (descending)
        for (List<Chat> chatList : groupedChats.values()) {
            chatList.sort(Comparator.comparing(Chat::getModifiedAt).reversed());
        }

        // Calculate maxOffset
        for (DateGroup group : DateGroup.values()) {
            List<Chat> chatList = groupedChats.get(group);
            if (!chatList.isEmpty()) {
                maxOffset += 19; // LINE_HEIGHT for group title
                maxOffset += chatList.size() * 19; // LINE_HEIGHT per chat
                if (group != DateGroup.OLDER) {
                    maxOffset += 10; // Extra spacing for non-OLDER groups
                }
            }
        }

        return new Pair<>(maxOffset, groupedChats);
    }

    private static DateGroup classify(long timestamp) {
        if (timestamp >= GROUP_THRESHOLDS.get(DateGroup.TODAY)) return DateGroup.TODAY;
        if (timestamp >= GROUP_THRESHOLDS.get(DateGroup.YESTERDAY)) return DateGroup.YESTERDAY;
        if (timestamp >= GROUP_THRESHOLDS.get(DateGroup.THIS_WEEK)) return DateGroup.THIS_WEEK;
        if (timestamp >= GROUP_THRESHOLDS.get(DateGroup.THIS_MONTH)) return DateGroup.THIS_MONTH;
        return DateGroup.OLDER;
    }

    private static Map<DateGroup, Long> computeThresholds(long nowMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(nowMillis);

        Map<DateGroup, Long> thresholds = new EnumMap<>(DateGroup.class);

        // TODAY
        resetToStartOfDay(cal);
        thresholds.put(DateGroup.TODAY, cal.getTimeInMillis());

        // YESTERDAY
        cal.add(Calendar.DAY_OF_MONTH, -1);
        thresholds.put(DateGroup.YESTERDAY, cal.getTimeInMillis());

        // THIS_WEEK
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        thresholds.put(DateGroup.THIS_WEEK, cal.getTimeInMillis());

        // THIS_MONTH
        cal.set(Calendar.DAY_OF_MONTH, 1);
        thresholds.put(DateGroup.THIS_MONTH, cal.getTimeInMillis());

        // OLDER is implicit
        return thresholds;
    }

    private static void resetToStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
