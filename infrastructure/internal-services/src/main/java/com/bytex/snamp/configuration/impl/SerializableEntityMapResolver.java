package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityMapResolver;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface SerializableEntityMapResolver<I extends SerializableEntityConfiguration, O extends SerializableEntityConfiguration> extends EntityMapResolver<I, O> {
    @Nonnull
    @Override
    SerializableEntityMap<O> apply(@Nonnull final I owner);
}
