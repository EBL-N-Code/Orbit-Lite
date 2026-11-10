package org.orbit.chat;

import com.google.gson.annotations.SerializedName;

public record ChatSummary(
        @SerializedName("chat_uuid") String uuid,
        @SerializedName("chat_name") String name,
        @SerializedName("updated_at") long updatedAt
) {
    public static ChatSummary fromChat(Chat chat) {
        return new ChatSummary(chat.getUuid(), chat.getName(), chat.getUpdatedAt());
    }
}