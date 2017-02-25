package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Map;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * Abstract configuration parser.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractConfigurationParser<E extends SerializableEntityConfiguration> implements Constants {

    abstract void removeAll(final ConfigurationAdmin admin) throws IOException;

    abstract void fill(final ConfigurationAdmin source, final Map<String, E> dest) throws IOException;

    abstract void saveChanges(final SerializableAgentConfiguration source, final ConfigurationAdmin dest) throws IOException;

    abstract Map<String, E> parse(final Dictionary<String, ?> config) throws IOException;

    static <E extends SerializableEntityConfiguration> E deserialize(final String itemName,
                                                                        final Class<E> entityType,
                                                                        final Dictionary<String, ?> properties,
                                                                        final ClassLoader caller) throws IOException {
        final byte[] serializedConfig = getValue(properties, itemName, byte[].class).orElseGet(ArrayUtils::emptyByteArray);
        return IOUtils.deserialize(serializedConfig, entityType, caller);
    }

    static <E extends EntityConfiguration & Serializable> byte[] serialize(final E input) throws IOException {
        return IOUtils.serialize(input);
    }
}
