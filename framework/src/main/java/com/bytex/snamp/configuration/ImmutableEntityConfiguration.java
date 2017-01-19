package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Represents read-only copy of the entity configuration.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
abstract class ImmutableEntityConfiguration extends HashMap<String, String> implements EntityConfiguration {
    private static final long serialVersionUID = -4249760782869284202L;

    ImmutableEntityConfiguration(final EntityConfiguration entity){
        super(entity);
    }

    ImmutableEntityConfiguration(){

    }

    @Override
    public abstract ImmutableEntityConfiguration asReadOnly();

    @Override
    public final String merge(final String key, final String value, final BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String put(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putAll(@Nonnull final Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String putIfAbsent(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean remove(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean replace(final String key, final String oldValue, final String newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final String replace(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void replaceAll(final BiFunction<? super String, ? super String, ? extends String> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDescription(final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void load(final Map<String, String> parameters) {
        throw new UnsupportedOperationException();
    }
}
