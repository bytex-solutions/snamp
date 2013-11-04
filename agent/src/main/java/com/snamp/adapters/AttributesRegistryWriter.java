package com.snamp.adapters;

import com.snamp.*;
import com.snamp.connectors.ManagementConnector;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.util.Collection;
import java.util.Map;

/**
 * Represents utility interface for connecting attributes.
 * @author roman
 */
public interface AttributesRegistryWriter {
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> putAll(final ManagementConnector connector, final String prefix, final Map<String, AttributeConfiguration> attributes);

}
