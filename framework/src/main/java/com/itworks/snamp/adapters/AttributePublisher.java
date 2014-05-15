package com.itworks.snamp.adapters;

import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.internal.semantics.ThreadSafe;

import java.util.Map;

/**
 * Represents an interface that must be implemented by every adapter and provides
 * publishing the management managementAttributes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributePublisher {
    /**
     * Exposes management managementAttributes.
     * @param connector The management connector that provides access to the specified managementAttributes.
     * @param namespace The managementAttributes namespace.
     * @param attributes The dictionary of managementAttributes.
     */
    @ThreadSafe(false)
    public void exposeAttributes(final AttributeSupport connector, final String namespace, final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes);
}
