package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.Box;
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

    static MessageType get(final TMessage message, final Box<String> entityName) {
        if (message.name.startsWith(GETTER_MESSAGE)) {
            entityName.set(GETTER_PREFIX.matcher(message.name).replaceFirst(""));
            return GET_ATTRIBUTE;
        } else if (message.name.startsWith(SETTER_MESSAGE)) {
            entityName.set(SETTER_PREFIX.matcher(message.name).replaceFirst(""));
            return SET_ATTRIBUTE;
        } else if (message.name.startsWith(NOTIFY_MESSAGE)) {
            entityName.set(NOTIFY_PREFIX.matcher(message.name).replaceFirst(""));
            return SEND_NOTIFICATION;
        } else return null;
    }
}
