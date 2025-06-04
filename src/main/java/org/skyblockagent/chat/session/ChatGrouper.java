package org.skyblockagent.chat.session;

import net.minecraft.util.Pair;

import java.util.*;

public class ChatGrouper {
    private static final Map<DateGroup, Long> GROUP_THRESHOLDS = computeThresholds(System.currentTimeMillis());
    private static final int LINE_HEIGHT = 19;

    public static Pair<Integer, List<Line>> groupChats(List<Chat> chats) {
        EnumSet<DateGroup> dateGroups = EnumSet.noneOf(DateGroup.class);
        List<Line> lines = new ArrayList<>();
        int maxOffset = 0;

        for (Chat chat : chats) {
            DateGroup group = classify(chat.getLastModified());
            maxOffset += LINE_HEIGHT;
            if (dateGroups.add(group)) {
                lines.add(new Line(true, group, null));
                maxOffset += LINE_HEIGHT + (group != DateGroup.OLDER ? 10 : 0);
            }
            lines.add(new Line(false, null, chat));
        }
        return new Pair<>(maxOffset, lines);
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
