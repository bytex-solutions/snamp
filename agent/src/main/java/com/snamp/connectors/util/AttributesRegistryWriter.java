package com.snamp.connectors.util;

import com.snamp.connectors.AttributeSupport;
import com.snamp.internal.Internal;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;

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
    public Collection<String> putAll(final AttributeSupport connector, final String prefix, final Map<String, AttributeConfiguration> attributes);

    public void clear();
}
