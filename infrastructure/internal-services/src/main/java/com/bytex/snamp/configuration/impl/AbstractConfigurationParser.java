package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

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

    abstract void removeAll(final ConfigurationAdmin admin) throws IOException;

    static void removeAll(final ConfigurationAdmin admin, final String filter) throws IOException {
        forEachConfiguration(admin, filter, Configuration::delete);
    }

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

    private static void fillProperties(final Dictionary<String, ?> input,
                                                      final Map<String, String> output,
                                                      final Set<String> ignoredProperties) {
        final Enumeration<String> names = input.keys();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            if (ignoredProperties.contains(name) || IGNORED_PROPERTIES.contains(name))
                continue;
            final Object value = input.get(name);
            if (value != null)
                output.put(name, value.toString());
        }
    }

    static void fillProperties(final Dictionary<String, ?> input,
                               final Map<String, String> output,
                               final String... ignoredProperties){
        fillProperties(input, output, ImmutableSet.copyOf(ignoredProperties));
    }

    static <E extends Exception> void forEachConfiguration(final ConfigurationAdmin admin,
                                                                     final String filter,
                                                                     final Acceptor<Configuration, E> reader) throws E, IOException {
        final Configuration[] configs;
        try {
            configs = admin.listConfigurations(filter);
        } catch (final InvalidSyntaxException e) {
            throw new IOException(e);
        }
        if (configs != null)
            for (final Configuration config : configs)
                reader.accept(config);
    }
}
