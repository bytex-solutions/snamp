package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.EntryReader;

/**
 * Represents reader for a set of events stored inside of the gateway instance.
 * @param <TAccessor> Type of the notification accessor.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface NotificationSet<TAccessor extends NotificationAccessor> {
    /**
     * Iterates over all notifications in this set.
     * @param notificationReader Notification reader.
     * @param <E> Type of the exception that can be thrown by the reader.
     * @return {@literal false}, if iteration was aborted.
     * @throws E Unable to process notification.
     */
    <E extends Throwable> boolean forEachNotification(final EntryReader<String, ? super TAccessor, E> notificationReader) throws E;
}
