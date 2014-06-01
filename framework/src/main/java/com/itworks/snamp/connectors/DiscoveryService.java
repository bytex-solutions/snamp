package com.itworks.snamp.connectors;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.ManagedEntity;
import com.itworks.snamp.core.FrameworkService;

import java.util.*;

/**
 * Additional service that can be exposed by {@link com.itworks.snamp.connectors.AbstractManagedResourceActivator}
 * class that provides discovery method for management attributes or notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface DiscoveryService extends FrameworkService {

    /**
     * Attempts to discover collection of managed entities (such as attributes or notifications)
     * using managed resource connection string.
     * <p>
     *     Do not add elements from the returned collection directly in {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#getElements(Class)}
     *     result set, use the following algorithm:
     *     <ul>
     *         <li>Create a new managed entity with {@link com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#newElement(Class)} method.</li>
     *         <li>Use {@link com.itworks.snamp.configuration.AbstractAgentConfiguration#copy(com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration, com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration)}
     *         or {@link com.itworks.snamp.configuration.AbstractAgentConfiguration#copy(com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration, com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration)} method
     *         to copy managed entity returned by this method into the newly created entity.</li>
     *     </ul>
     * </p>
     * @param connectionString Managed resource connection string.
     * @param connectionOptions Managed resource connection options.
     * @param entityType Type of the managed entity.
     * @param <T> Type of the managed entity.
     * @return A collection of discovered entities; or empty collection if nothing entities
     * was be detected.
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration
     * @see com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration
     */
    <T extends ManagedEntity> Collection<T> discover(final String connectionString,
                  final Map<String, String> connectionOptions,
                  final Class<T> entityType);
}
