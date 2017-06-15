package com.bytex.snamp.json;


import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ObjectNode;

import javax.management.Notification;
import java.io.IOException;
import java.util.Date;

/**
 * Provides serialization of {@link Notification} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NotificationSerializer extends JsonSerializer<Notification> {
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
    public void serialize(final Notification src, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        final ObjectNode node = ThreadLocalJsonFactory.getFactory().objectNode();
        node.put(SOURCE, ThreadLocalJsonFactory.getFactory().textNode(src.getSource().toString()));
        node.put(NOTIF_TYPE, src.getType());
        node.put(SEQUENCE_NUMBER, src.getSequenceNumber());
        if (timeStampAsString)
            node.put(TIME_STAMP, ThreadLocalJsonFactory.toValueNode(new Date(src.getTimeStamp())));
        else
            node.put(TIME_STAMP, src.getTimeStamp());
        node.put(MESSAGE, src.getMessage());
        node.put(USER_DATA, ThreadLocalJsonFactory.toValueNode(src.getUserData()));
        node.serialize(jgen, provider);
    }
}
