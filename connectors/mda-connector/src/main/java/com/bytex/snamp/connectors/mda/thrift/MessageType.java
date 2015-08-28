package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.Consumer;
import org.apache.thrift.protocol.TMessage;

import java.util.regex.Pattern;

/**
 * Represents Thrift message type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
enum MessageType {
    GET_ATTRIBUTE,
    SET_ATTRIBUTE,
    SEND_NOTIFICATION;

    private static final String GETTER_MESSAGE = "get_";
    private static final Pattern GETTER_PREFIX = Pattern.compile(GETTER_MESSAGE);

    private static final String SETTER_MESSAGE = "set_";
    private static final Pattern SETTER_PREFIX = Pattern.compile(SETTER_MESSAGE);

    private static final String NOTIFY_MESSAGE = "notify_";
    private static final Pattern NOTIFY_PREFIX = Pattern.compile(NOTIFY_MESSAGE);

    /**
     * Parses message type.
     * @param message Input message to be processed.
     * @param entityName A name of the attribute or notification extracted from the message.
     * @return Message type.
     * @throws E Unable to set entity name.
     */
    static <E extends Throwable> MessageType get(final TMessage message, final Consumer<String, E> entityName) throws E{
        if (message.name.startsWith(GETTER_MESSAGE)) {
            entityName.accept(GETTER_PREFIX.matcher(message.name).replaceFirst(""));
            return GET_ATTRIBUTE;
        } else if (message.name.startsWith(SETTER_MESSAGE)) {
            entityName.accept(SETTER_PREFIX.matcher(message.name).replaceFirst(""));
            return SET_ATTRIBUTE;
        } else if (message.name.startsWith(NOTIFY_MESSAGE)) {
            entityName.accept(NOTIFY_PREFIX.matcher(message.name).replaceFirst(""));
            return SEND_NOTIFICATION;
        } else return null;
    }
}
