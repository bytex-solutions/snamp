package com.bytex.snamp.core;

import com.bytex.snamp.ArrayUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * Represents a reference to a local service available through {@link java.util.ServiceLoader} class.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
final class LocalServiceReference<S> implements ServiceReference<S>, Supplier<S> {
    private final S serviceImpl;

    private LocalServiceReference(final S service){
        this.serviceImpl = Objects.requireNonNull(service);
    }

    static <S> Optional<LocalServiceReference<S>> resolve(final ClassLoader context, final Class<S> serviceType) {
        final ServiceLoader<S> loader = ServiceLoader.load(serviceType, context);
        return StreamSupport.stream(loader.spliterator(), false)
                .findFirst()
                .map(LocalServiceReference::new);
    }

    @Override
    public Object getProperty(final String key) {
        return null;
    }

    @Override
    public String[] getPropertyKeys() {
        return ArrayUtils.emptyArray(String[].class);
    }

    @Override
    public Bundle getBundle() {
        return null;
    }

    @Override
    public Bundle[] getUsingBundles() {
        return ArrayUtils.emptyArray(Bundle[].class);
    }

    @Override
    public boolean isAssignableTo(final Bundle bundle, final String className) {
        return false;
    }

    @Override
    public int compareTo(final Object reference) {
        return reference instanceof LocalServiceReference<?> ? 0 : -1;
    }

    @Override
    public S get() {
        return serviceImpl;
    }
}
