package com.bytex.snamp.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents read-only copy of the feature configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
abstract class ImmutableManagedResourceTemplate extends ImmutableTypedEntityConfiguration implements ManagedResourceTemplate {
    private static final class ImmutableEntityMap<T extends FeatureConfiguration> extends HashMap<String, T> implements EntityMap<T>{
        private static final long serialVersionUID = 7742688871979172349L;

        private <S extends FeatureConfiguration> ImmutableEntityMap(final EntityMap<? extends S> entities, final Function<? super S, ? extends T> mapper) {
            entities.forEach((id, entity) -> super.put(id, mapper.apply(entity)));
        }

        @Override
        public T put(final String key, final T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(final Map<? extends String, ? extends T> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T remove(final Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T putIfAbsent(final String key, final T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(final Object key, final Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean replace(final String key, final T oldValue, final T newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T replace(final String key, final T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T merge(final String key, final T value, final BiFunction<? super T, ? super T, ? extends T> remappingFunction) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(final BiFunction<? super String, ? super T, ? extends T> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAndConsume(final String entityID, final Consumer<? super T> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <I> boolean addAndConsume(final I input, final String entityID, final BiConsumer<? super I, ? super T> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T getOrAdd(final String entityID) {
            throw new UnsupportedOperationException();
        }
    }

    private static final long serialVersionUID = 6678458559328627073L;
    private final ImmutableEntityMap<AttributeConfiguration> attributes;
    private final ImmutableEntityMap<EventConfiguration> events;
    private final ImmutableEntityMap<OperationConfiguration> operations;

    ImmutableManagedResourceTemplate(final ManagedResourceTemplate entity) {
        super(entity);
        attributes = new ImmutableEntityMap<>(entity.getFeatures(AttributeConfiguration.class), AttributeConfiguration::asReadOnly);
        events = new ImmutableEntityMap<>(entity.getFeatures(EventConfiguration.class), EventConfiguration::asReadOnly);
        operations = new ImmutableEntityMap<>(entity.getFeatures(OperationConfiguration.class), OperationConfiguration::asReadOnly);
    }

    @Override
    public abstract ImmutableManagedResourceTemplate asReadOnly();

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends FeatureConfiguration> EntityMap<? extends T> getFeatures(final Class<T> featureType) {
        final ImmutableEntityMap result;
        if(featureType == null)
            result = null;
        else if(featureType.isAssignableFrom(AttributeConfiguration.class))
            result = attributes;
        else if(featureType.isAssignableFrom(EventConfiguration.class))
            result = events;
        else if(featureType.isAssignableFrom(OperationConfiguration.class))
            result = operations;
        else
            result = null;
        return result;
    }
}
