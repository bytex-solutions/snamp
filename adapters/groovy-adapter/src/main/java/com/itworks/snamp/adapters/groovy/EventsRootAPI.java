package com.itworks.snamp.adapters.groovy;

import com.itworks.snamp.adapters.modeling.NotificationAccessor;
import com.itworks.snamp.internal.RecordReader;

/**
 * Represents root-level DSL for working with events.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface EventsRootAPI {
    ResourceNotificationsAnalyzer eventsAnalyzer();
    <E extends Exception> void processEvents(final RecordReader<String, NotificationAccessor, E> closure) throws E;
}
