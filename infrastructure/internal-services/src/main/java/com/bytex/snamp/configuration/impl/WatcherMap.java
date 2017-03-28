package com.bytex.snamp.configuration.impl;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WatcherMap extends SerializableEntityMap<SerializableSupervisorConfiguration> {
    private static final long serialVersionUID = -1512394714178712018L;

    @Override
    SerializableSupervisorConfiguration createValue() {
        final SerializableSupervisorConfiguration result = new SerializableSupervisorConfiguration();
        result.markAsModified();
        return result;
    }
}
