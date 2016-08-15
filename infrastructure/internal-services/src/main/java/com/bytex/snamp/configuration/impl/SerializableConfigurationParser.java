package com.bytex.snamp.configuration.impl;

import com.google.common.collect.ImmutableSet;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class SerializableConfigurationParser<E extends AbstractEntityConfiguration> extends AbstractConfigurationParser<E> {
    private final String persistenceID;
    private final Class<E> entityType;
    private final ImmutableSet<String> excludeConfigKeys;

    SerializableConfigurationParser(final String pid,
                                    final Class<E> entityType,
                                    final String... excludeConfigKeys){
        this.persistenceID = Objects.requireNonNull(pid);
        this.entityType = Objects.requireNonNull(entityType);
        this.excludeConfigKeys = ImmutableSet.copyOf(excludeConfigKeys);
    }

    private Configuration getConfig(final ConfigurationAdmin admin) throws IOException{
        return admin.getConfiguration(persistenceID);
    }

    private E deserialize(final String itemName, final Dictionary<String, ?> properties) throws IOException {
        return deserialize(itemName, entityType, properties, getClass().getClassLoader());
    }

    @Override
    final void removeAll(final ConfigurationAdmin admin) throws IOException {
        getConfig(admin).delete();
    }

    private void readItems(final Configuration input, final Map<String, E> output) throws IOException {
        final Dictionary<String, ?> items = input.getProperties();
        final Enumeration<String> names = items == null ? EmptyStringEnumerator.getInstance() : items.keys();
        while (names.hasMoreElements()) {
            final String itemName = names.nextElement();
            if (!excludeConfigKeys.contains(itemName))
                switch (itemName) {
                    case SERVICE_PID:
                    case OBJECTCLASS:
                        continue;
                    default:
                        if (!excludeConfigKeys.contains(itemName))
                            output.put(itemName, deserialize(itemName, items));
                }
        }
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
