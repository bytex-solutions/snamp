package com.itworks.snamp.adapters;

import com.itworks.snamp.internal.semantics.ThreadSafe;
import com.itworks.snamp.connectors.NotificationSupport;
import com.itworks.snamp.configuration.AgentConfiguration;

import java.util.Map;

/**
 * Represents an interface that may be implemented by the adapter which supports
 * management notification publishing.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface NotificationPublisher {
    /**
     * Exposes monitoring events.
     * @param connector The management connector that provides notification listening and subscribing.
     * @param namespace The events namespace.
     * @param events The collection of configured notifications.
     */
    @ThreadSafe(false)
    public void exposeEvents(final NotificationSupport connector, final String namespace, final Map<String, AgentConfiguration.ManagementTargetConfiguration.EventConfiguration> events);
}
