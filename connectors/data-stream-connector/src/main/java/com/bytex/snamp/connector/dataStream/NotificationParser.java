package com.bytex.snamp.connector.dataStream;

import javax.management.Notification;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents notification parser.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface NotificationParser {
    /**
     * Converts headers and body into stream of {@link Notification}s.
     * @param headers A headers of the input message. Cannot be {@literal null}.
     * @param body Body of the message.
     * @return Stream of notifications restored from the message headers and body.
     * @throws Exception Unable to parse notification.
     */
    Stream<Notification> parse(final Map<String, ?> headers, final Object body) throws Exception;
}
