package com.bytex.snamp.gateway.http;

import com.google.common.collect.ImmutableMap;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.0
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

    <V> AtmosphereObjectFactoryBuilder add(final Class<V> classType,
                                                  final Callable<? extends V> factory){
        factories.put(classType, factory);
        return this;
    }

    @SuppressWarnings("unchecked")
    <V> AtmosphereObjectFactoryBuilder add(final Class<V> classType,
                                                  final Supplier<? extends V> factory) {
        return add(classType, (Callable<? extends V>) factory::get);
    }

    AtmosphereObjectFactory build(){
        return createFactory(factories.build());
    }

    @Override
    public AtmosphereObjectFactory get() {
        return build();
    }
}
