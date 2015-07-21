package com.itworks.snamp.adapters.modeling;

import com.itworks.snamp.internal.RecordReader;

/**
 * Represents reader for a set of events stored inside of the resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationSet<TAccessor extends NotificationAccessor> {
    <E extends Exception> void forEachNotification(final RecordReader<String, ? super TAccessor, E> notificationReader) throws E;
}
