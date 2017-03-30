package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityMap;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract configuration parser.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractConfigurationParser<E extends SerializableEntityConfiguration> implements Constants {
    static final ImmutableSet<String> IGNORED_PROPERTIES = ImmutableSet.of(SERVICE_PID,
            OBJECTCLASS,
            ConfigurationAdmin.SERVICE_FACTORYPID,
            ConfigurationAdmin.SERVICE_BUNDLELOCATION);

    private final SerializableEntityMapResolver<SerializableAgentConfiguration, E> entityMapResolver;

    AbstractConfigurationParser(final SerializableEntityMapResolver<SerializableAgentConfiguration, E> resolver){
        entityMapResolver = Objects.requireNonNull(resolver);
    }

    abstract void removeAll(final ConfigurationAdmin admin) throws IOException;

    abstract void populateRepository(final ConfigurationAdmin source, final EntityMap<E> dest) throws IOException;

    final void populateRepository(final ConfigurationAdmin source, final SerializableAgentConfiguration dest) throws IOException{
        populateRepository(source, entityMapResolver.apply(dest));
    }

    abstract void saveChanges(final SerializableEntityMap<E> source, final ConfigurationAdmin dest) throws IOException;

    final void saveChanges(final SerializableAgentConfiguration source, final ConfigurationAdmin dest) throws IOException {
        saveChanges(entityMapResolver.apply(source), dest);
    }

    abstract Map<String, E> parse(final Dictionary<String, ?> config) throws IOException;
}
