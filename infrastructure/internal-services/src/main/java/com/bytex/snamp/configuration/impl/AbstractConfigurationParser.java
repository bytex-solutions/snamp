package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static com.bytex.snamp.MapUtils.getValue;

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

    final Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
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

    final <R extends Serializable> R deserialize(final String itemName,
                                                 final TypeToken<R> entityType,
                                                 final Dictionary<String, ?> properties) throws IOException {
        final byte[] serializedConfig = getValue(properties, itemName, byte[].class).orElseGet(ArrayUtils::emptyByteArray);
        return IOUtils.deserialize(serializedConfig, entityType, getClass().getClassLoader());
    }

    final <R extends Serializable> R deserialize(final String itemName,
                                                 final Class<R> entityType,
                                                 final Dictionary<String, ?> properties) throws IOException {
        return deserialize(itemName, TypeToken.of(entityType), properties);
    }
}
