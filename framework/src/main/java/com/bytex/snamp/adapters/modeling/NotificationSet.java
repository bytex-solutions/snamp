package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.internal.EntryReader;

/**
 * Represents reader for a set of events stored inside of the resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationSet<TAccessor extends NotificationAccessor> {
    <E extends Exception> void forEachNotification(final EntryReader<String, ? super TAccessor, E> notificationReader) throws E;
}
