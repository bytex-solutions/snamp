package com.bytex.snamp.adapters.http;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;

import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class AtmosphereObjectFactoryBuilder implements Supplier<AtmosphereObjectFactory> {
    private final ImmutableMap.Builder<Class<?>, Callable> factories = ImmutableMap.builder();

    private static AtmosphereObjectFactory createFactory(final ImmutableMap<Class<?>, Callable> factories){
        return new AtmosphereObjectFactory() {
            @Override
            public <T, U extends T> T newClassInstance(final AtmosphereFramework framework,
                                                       final Class<T> classType,
                                                       final Class<U> defaultType) throws InstantiationException, IllegalAccessException {
                if(factories.containsKey(classType))
                    try {
                        return classType.cast(factories.get(classType).call());
                    } catch (final Exception e) {
                        throw new InstantiationException(e.getMessage());
                    }
                else return defaultType.newInstance();
            }
        };
    }

    public <V> AtmosphereObjectFactoryBuilder add(final Class<V> classType,
                                                  final Callable<? extends V> factory){
        factories.put(classType, factory);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <V> AtmosphereObjectFactoryBuilder add(final Class<V> classType,
                                                  final Supplier<? extends V> factory) {
        return add(classType, (Callable<? extends V>) factory::get);
    }

    public AtmosphereObjectFactory build(){
        return createFactory(factories.build());
    }

    @Override
    public AtmosphereObjectFactory get() {
        return build();
    }
}
