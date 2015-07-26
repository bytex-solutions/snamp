package com.bytex.snamp.adapters.groovy.dsl;

import javax.management.MBeanNotificationInfo;
import java.util.Collection;
import java.util.Set;

/**
 * Provides access to events.
 */
interface EventsView {
    Collection<MBeanNotificationInfo> getEventsMetadata(final String resourceName);

    Set<String> getEvents(final String resourceName);
}
