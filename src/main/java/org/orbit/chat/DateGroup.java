package org.orbit.chat;

public enum DateGroup {
    TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, OLDER;

    public String getName() {
        return switch (this) {
            case TODAY -> "Today";
            case YESTERDAY -> "Yesterday";
            case THIS_WEEK -> "This Week";
            case THIS_MONTH -> "This Month";
            case OLDER -> "Older";
        };
    }
}
