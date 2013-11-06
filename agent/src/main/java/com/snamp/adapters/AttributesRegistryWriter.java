package com.snamp.adapters;

import com.snamp.*;
import com.snamp.connectors.ManagementConnector;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.util.Collection;
import java.util.Map;

/**
 * Represents utility interface for connecting attributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface AttributesRegistryWriter {
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> putAll(final ManagementConnector connector, final String prefix, final Map<String, AttributeConfiguration> attributes);

}
