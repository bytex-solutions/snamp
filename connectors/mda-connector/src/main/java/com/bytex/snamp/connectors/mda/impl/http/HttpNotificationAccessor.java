package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.connectors.mda.MDANotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.management.openmbean.OpenDataException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpNotificationAccessor extends MDANotificationInfo {
    private static final String MESSAGE_FIELD  = "message";
    private static final String TIME_STAMP_FIELD = "timeStamp";
    private static final String SEQ_NUM_FIELD = "sequenceNumber";
    private static final String DATA_FIELD = "data";
    private static final long serialVersionUID = -2119558529607416756L;

    private final AtomicLong sequenceNumberCounter;

    HttpNotificationAccessor(final String notifType,
                             final NotificationDescriptor descriptor) {
        super(notifType, descriptor.getDescription(descriptor.getNotificationCategory()), descriptor);
        sequenceNumberCounter = new AtomicLong(0L);
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
        if ((notification.has(DATA_FIELD) || !notification.get(DATA_FIELD).isJsonNull()) && getAttachmentType() != null) {
            return JsonDataConverter.deserialize(formatter, getAttachmentType(), notification.get(DATA_FIELD));
        } else return null;
    }
}
