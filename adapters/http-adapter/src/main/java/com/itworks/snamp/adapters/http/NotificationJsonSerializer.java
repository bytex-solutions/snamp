package com.itworks.snamp.adapters.http;

import com.google.gson.*;

import javax.management.Notification;
import java.lang.reflect.Type;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationJsonSerializer implements JsonSerializer<Notification> {
    private static final String NOTIF_TYPE = "type";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";
    private static final String TIME_STAMP = "timeStamp";
    private static final String MESSAGE = "message";
    private static final String USER_DATA = "userData";

    static JsonObject serialize(final Notification src, final JsonSerializationContext context){
        final JsonObject result = new JsonObject();
        result.add(NOTIF_TYPE, context.serialize(src.getType()));
        result.add(SEQUENCE_NUMBER, context.serialize(src.getSequenceNumber()));
        result.add(TIME_STAMP, context.serialize(src.getTimeStamp()));
        result.add(MESSAGE, context.serialize(src.getMessage()));
        final Object data = src.getUserData();
        result.add(USER_DATA, data != null ? context.serialize(data) : JsonNull.INSTANCE);
        return result;
    }

    @Override
    public JsonElement serialize(final Notification src, final Type typeOfSrc, final JsonSerializationContext context) {
        return serialize(src, context);
    }
}
