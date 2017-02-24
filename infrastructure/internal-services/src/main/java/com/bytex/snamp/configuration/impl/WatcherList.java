package com.bytex.snamp.configuration.impl;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WatcherList extends ConfigurationEntityList<SerializableManagedResourceGroupWatcherConfiguration> {
    private static final long serialVersionUID = -1512394714178712018L;

    @Override
    SerializableManagedResourceGroupWatcherConfiguration createValue() {
        final SerializableManagedResourceGroupWatcherConfiguration result = new SerializableManagedResourceGroupWatcherConfiguration();
        result.markAsModified();
        return result;
    }
}
