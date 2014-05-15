package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.internal.semantics.Internal;
import com.itworks.snamp.internal.semantics.ThreadSafe;

import java.util.Collection;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Represents utility interface for connecting managementAttributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface AttributesRegistryWriter {
    @ThreadSafe
    public Collection<String> putAll(final AttributeSupport connector, final String prefix, final Map<String, AttributeConfiguration> attributes);

    public void clear();

    /**
     * Disconnects all managementAttributes.
     */
    public void disconnect();
}
