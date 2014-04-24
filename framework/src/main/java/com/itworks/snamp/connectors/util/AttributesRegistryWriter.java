package com.itworks.snamp.connectors.util;

import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.internal.Internal;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.util.Collection;
import java.util.Map;

/**
 * Represents utility interface for connecting managementAttributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface AttributesRegistryWriter {
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> putAll(final AttributeSupport connector, final String prefix, final Map<String, AttributeConfiguration> attributes);

    public void clear();

    /**
     * Disconnects all managementAttributes.
     */
    public void disconnect();
}
