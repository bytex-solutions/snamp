package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

/**
 * Represents a set of configured thread pools.
 * @since 2.0
 * @version 2.0
 */
final class ThreadPoolRegistry extends ConfigurationEntityRegistry<SerializableThreadPoolConfiguration> {

    @SpecialUse
    public ThreadPoolRegistry(){

    }

    @Override
    protected SerializableThreadPoolConfiguration createEntity() {
        final SerializableThreadPoolConfiguration result = new SerializableThreadPoolConfiguration();
        result.markAsModified();
        return result;
    }
}
