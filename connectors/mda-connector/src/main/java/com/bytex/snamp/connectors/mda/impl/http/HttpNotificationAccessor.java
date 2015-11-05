package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.connectors.notifications.CustomNotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.management.openmbean.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpNotificationAccessor extends CustomNotificationInfo {
    private static final String MESSAGE_FIELD  = "message";
    private static final String TIME_STAMP_FIELD = "timeStamp";
    private static final String SEQ_NUM_FIELD = "sequenceNumber";
    private static final String DATA_FIELD = "data";
    private static final long serialVersionUID = -2119558529607416756L;

    private final AtomicLong sequenceNumberCounter;
    private final HttpValueParser attachmentParser;

    HttpNotificationAccessor(final String notifType,
                             final OpenType<?> attachmentType,
                             final NotificationDescriptor descriptor) throws OpenDataException {
        super(notifType, descriptor.getDescription(descriptor.getNotificationCategory()), descriptor);
        sequenceNumberCounter = new AtomicLong(0L);
        if(attachmentType instanceof SimpleType<?> || attachmentType instanceof ArrayType<?>)
            attachmentParser = new SimpleValueParser(WellKnownType.getType(attachmentType));
        else if(attachmentType instanceof CompositeType)
            attachmentParser = new CompositeValueParser((CompositeType)attachmentType);
        else attachmentParser = FallbackValueParser.INSTANCE;
    }

    static String getMessage(final JsonObject notification){
        return notification.has(MESSAGE_FIELD) ? notification.get(MESSAGE_FIELD).getAsString() : "";
    }

    static long getTimeStamp(final JsonObject notification){
        return notification.has(TIME_STAMP_FIELD) ? notification.get(TIME_STAMP_FIELD).getAsLong() : System.currentTimeMillis();
    }

    long getSequenceNumber(final JsonObject notification){
        return notification.has(SEQ_NUM_FIELD) ?
                notification.get(SEQ_NUM_FIELD).getAsLong() : sequenceNumberCounter.getAndIncrement();
    }

    Object getUserData(final JsonObject notification, final Gson formatter) throws OpenDataException {
        if(notification.has(DATA_FIELD) || !notification.get(DATA_FIELD).isJsonNull()){
            return attachmentParser.deserialize(notification.get(DATA_FIELD), formatter);
        }
        else return null;
    }
}
