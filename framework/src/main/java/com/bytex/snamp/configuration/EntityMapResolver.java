package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Represents functional interface used to extract entity map from its owner.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface EntityMapResolver<I extends EntityConfiguration, O extends EntityConfiguration> extends Function<I, EntityMap<? extends O>> {
    @Override
    @Nonnull
    EntityMap<? extends O> apply(@Nonnull final I owner);
}
