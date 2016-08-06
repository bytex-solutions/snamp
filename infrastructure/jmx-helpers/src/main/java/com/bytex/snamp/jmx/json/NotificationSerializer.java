package com.bytex.snamp.jmx.json;

import com.google.gson.*;

import javax.management.Notification;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NotificationSerializer implements JsonSerializer<Notification> {
    private static final String NOTIF_TYPE = "type";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";
    private static final String TIME_STAMP = "timeStamp";
    private static final String MESSAGE = "message";
    private static final String USER_DATA = "userData";
    private static final String SOURCE = "source";

    private final boolean timeStampAsString;

    public NotificationSerializer(){
        this(true);
    }

    public NotificationSerializer(final boolean timeStampAsString){
        this.timeStampAsString = timeStampAsString;
    }

    @Override
    public JsonElement serialize(final Notification src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject result = new JsonObject();
        result.add(SOURCE, context.serialize(src.getSource()));
        result.add(NOTIF_TYPE, context.serialize(src.getType()));
        result.add(SEQUENCE_NUMBER, context.serialize(src.getSequenceNumber()));
        final Object timeStamp;
        if(timeStampAsString)
            timeStamp = new Date(src.getTimeStamp());
        else
            timeStamp = src.getTimeStamp();
        result.add(TIME_STAMP, context.serialize(timeStamp));
        result.add(MESSAGE, context.serialize(src.getMessage()));
        final Object data = src.getUserData();
        result.add(USER_DATA, data != null ? context.serialize(data) : JsonNull.INSTANCE);
        return result;
    }
}
