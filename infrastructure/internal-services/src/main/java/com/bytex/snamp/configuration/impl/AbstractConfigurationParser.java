package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityConfiguration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Map;

/**
 * Abstract configuration parser.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractConfigurationParser<E extends EntityConfiguration> {
    abstract void removeAll(final ConfigurationAdmin admin) throws IOException;

    abstract void fill(final ConfigurationAdmin source, final Map<String, E> dest) throws IOException;

    abstract void saveChanges(final SerializableAgentConfiguration source, final ConfigurationAdmin dest) throws IOException;
}
