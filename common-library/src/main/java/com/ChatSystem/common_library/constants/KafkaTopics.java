package com.ChatSystem.common_library.constants;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String USER_CREATED        = "user.created";
    public static final String MESSAGE_CREATED     = "message.created";
    public static final String MESSAGE_DELIVERED   = "message.delivered";
    public static final String MESSAGE_SEEN        = "message.seen";
    public static final String PRESENCE_UPDATED    = "presence.updated";
    public static final String USER_DELETED        = "user.deleted";
}
