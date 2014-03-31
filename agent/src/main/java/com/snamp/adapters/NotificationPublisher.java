package com.snamp.adapters;

import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;
import com.snamp.connectors.NotificationSupport;
import com.snamp.configuration.AgentConfiguration;

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
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public void exposeEvents(final NotificationSupport connector, final String namespace, final Map<String, AgentConfiguration.ManagementTargetConfiguration.EventConfiguration> events);
}
