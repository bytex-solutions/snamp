package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CMManagedResourceGroupParser extends AbstractConfigurationParser<SerializableManagedResourceGroupConfiguration> {
    private static final String PID = "com.bytex.snamp.connector.groups";

    private static final class ManagedResourceGroupConfigurationException extends PersistentConfigurationException{
        private static final long serialVersionUID = 2691397386799547187L;

        private ManagedResourceGroupConfigurationException(final Throwable e) {
            super(PID, SerializableManagedResourceGroupConfiguration.class, e);
        }
    }

    private static Configuration getConfig(final ConfigurationAdmin admin) throws IOException {
        return admin.getConfiguration(PID);
    }

    private static SerializableManagedResourceGroupConfiguration deserialize(final String groupName,
                                                                  final Dictionary<String, ?> properties,
                                                                  final ClassLoader caller) throws IOException {
        final byte[] serializedConfig = Utils.getProperty(properties, groupName, byte[].class, (Supplier<byte[]>) () -> new byte[0]);
        return IOUtils.deserialize(serializedConfig, SerializableManagedResourceGroupConfiguration.class, caller);
    }

    private void readGroups(final Configuration input, final Map<String, SerializableManagedResourceGroupConfiguration> output) throws IOException {
        final Dictionary<String, ?> groups = input.getProperties();
        final Enumeration<String> names = groups == null ? EmptyStringEnumerator.getInstance() : groups.keys();
        while (names.hasMoreElements()) {
            final String groupName = names.nextElement();
            switch (groupName) {
                case Constants.SERVICE_PID:
                case Constants.OBJECTCLASS:
                case ThreadPoolRepository.DEFAULT_POOL:
                    continue;
                default:
                    output.put(groupName, deserialize(groupName, groups, getClass().getClassLoader()));
            }
        }
    }

    private static byte[] serialize(final SerializableManagedResourceGroupConfiguration input) throws ManagedResourceGroupConfigurationException {
        try {
            return IOUtils.serialize(input);
        } catch (final IOException e) {
            throw new ManagedResourceGroupConfigurationException(e);
        }
    }

    @Override
    void removeAll(final ConfigurationAdmin admin) throws IOException {
        getConfig(admin).delete();
    }

    @Override
    void fill(final ConfigurationAdmin source, final Map<String, SerializableManagedResourceGroupConfiguration> dest) throws IOException {
        readGroups(getConfig(source), dest);
    }

    private static void saveChanges(final SerializableAgentConfiguration source, final Dictionary<String, Object> dest) throws IOException{
        final ConfigurationEntityList<? extends SerializableManagedResourceGroupConfiguration> groups = source.getEntities(SerializableManagedResourceGroupConfiguration.class);
        //remove deleted groups
        Collections.list(dest.keys()).stream()
                .filter(destGroupName -> !groups.containsKey(destGroupName))
                .forEach(dest::remove);
        //save modified groups
        groups.modifiedEntries((groupName, groupConfig) -> {
            dest.put(groupName, serialize(groupConfig));
            return true;
        });
    }

    @Override
    void saveChanges(final SerializableAgentConfiguration source, final ConfigurationAdmin dest) throws IOException {
        final Configuration config = getConfig(dest);
        Dictionary<String, Object> props = config.getProperties();
        if(props == null)
            props = new Hashtable<>();
        saveChanges(source, props);
        config.update(props);
    }
}
