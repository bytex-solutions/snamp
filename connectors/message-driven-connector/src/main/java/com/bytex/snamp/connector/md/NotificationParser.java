package com.bytex.snamp.connector.md;

import javax.management.Notification;
import java.util.Map;

/**
 * Represents notification parser.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface NotificationParser {
    /**
     * Converts headers and body into {@link Notification}.
     * @param headers A headers of the input message. Cannot be {@literal null}.
     * @param body Body of the message.
     * @return Notification restored from the message headers and body.
     * @throws Exception Unable to parse notification.
     */
    Notification parse(final Map<String, ?> headers, final Object body) throws Exception;
}
