package com.snamp.adapters;

import com.snamp.*;
import com.snamp.connectors.AttributeSupport;
import com.snamp.hosting.AgentConfiguration;

import java.util.Map;

/**
 * Represents an interface that must be implemented by every adapter and provides
 * publishing the management attributes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributePublisher {
    /**
     * Exposes management attributes.
     * @param connector The management connector that provides access to the specified attributes.
     * @param namespace The attributes namespace.
     * @param attributes The dictionary of attributes.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public void exposeAttributes(final AttributeSupport connector, final String namespace, final Map<String, AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration> attributes);
}
