package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

import javax.annotation.Nonnull;

/**
 * Represents a set of configured thread pools.
 * @since 2.0
 * @version 2.1
 */
final class ThreadPoolMap extends SerializableEntityMap<SerializableThreadPoolConfiguration> {
    private static final long serialVersionUID = 5480918847294476287L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ThreadPoolMap(){

    }

    @Override
    @Nonnull
    protected SerializableThreadPoolConfiguration createValue() {
        final SerializableThreadPoolConfiguration result = new SerializableThreadPoolConfiguration();
        result.markAsModified();
        return result;
    }
}
