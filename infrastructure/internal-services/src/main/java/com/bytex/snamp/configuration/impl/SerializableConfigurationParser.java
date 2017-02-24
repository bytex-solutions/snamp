package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Stateful;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SerializableConfigurationParser<E extends SerializableEntityConfiguration & Stateful> extends AbstractConfigurationParser<E> {
    final String persistentID;
    private final Class<E> entityType;
    private final ImmutableSet<String> excludeConfigKeys;

    SerializableConfigurationParser(final String pid,
                                    final Class<E> entityType,
                                    final String... excludeConfigKeys) {
        this.persistentID = Objects.requireNonNull(pid);
        this.entityType = Objects.requireNonNull(entityType);
        this.excludeConfigKeys = ImmutableSet.<String>builder()
                .add(excludeConfigKeys)
                .add(SERVICE_PID)
                .add(OBJECTCLASS)
                .build();
    }

    /**
     * Converts {@link Configuration} into SNAMP-specific configuration section.
     *
     * @param config Configuration to convert.
     * @return Converted SNAMP configuration section.
     * @throws IOException Unable to parse persistent configuration.
     */
    @Override
    public final Collection<E> parse(final Dictionary<String, ?> config) throws IOException {
        final List<E> result = new LinkedList<>();
        readItems(config, (key, value) -> result.add(value));
        return result;
    }

    private Configuration getConfig(final ConfigurationAdmin admin) throws IOException{
        return admin.getConfiguration(persistentID);
    }

    private E deserialize(final String itemName, final Dictionary<String, ?> properties) throws IOException {
        return deserialize(itemName, entityType, properties, getClass().getClassLoader());
    }

    @Override
    final void removeAll(final ConfigurationAdmin admin) throws IOException {
        getConfig(admin).delete();
    }

    private void readItems(final Dictionary<String, ?> items, final BiConsumer<? super String, ? super E> output) throws IOException {
        final Enumeration<String> keys = items.keys();
        while (keys.hasMoreElements()) {
            final String itemName = keys.nextElement();
            if (!excludeConfigKeys.contains(itemName))
                output.accept(itemName, deserialize(itemName, items));
        }
    }

    private void readItems(final Configuration input, final Map<String, E> output) throws IOException {
        final Dictionary<String, ?> items = input.getProperties();
        if (items != null)
            readItems(items, output::put);
    }

    @Override
    final void fill(final ConfigurationAdmin source, final Map<String, E> dest) throws IOException {
        readItems(getConfig(source), dest);
    }

    private void saveChanges(final SerializableAgentConfiguration source, final Dictionary<String, Object> dest) throws IOException {
        final ConfigurationEntityList<? extends E> list = source.getEntities(entityType);
        //remove deleted items
        Collections.list(dest.keys()).stream()
                .filter(destName -> !list.containsKey(destName))
                .forEach(dest::remove);
        //save modified items
        list.modifiedEntries((itemName, itemConfig) -> {
            dest.put(itemName, serialize(itemConfig));
            return true;
        });
    }

    @Override
    final void saveChanges(final SerializableAgentConfiguration source, final ConfigurationAdmin dest) throws IOException {
        final Configuration config = getConfig(dest);
        Dictionary<String, Object> props = config.getProperties();
        if(props == null)
            props = new Hashtable<>();
        saveChanges(source, props);
        config.update(props);
    }
}
