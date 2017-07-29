package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Represents point of service registration on OSGi Service Registry.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ServiceRegistrationHolder<S, T extends S> implements ServiceRegistration<S>, Supplier<T>, SafeCloseable {
    private final ServiceRegistration<?> registration;
    private T serviceInstance;

    ServiceRegistrationHolder(@Nonnull final BundleContext context,
                              @Nonnull final T service,
                              @Nonnull final Dictionary<String, ?> identity,
                              final Set<Class<? super T>> contracts) {
        if(contracts.isEmpty())
            throw new IllegalArgumentException("Cannot register service without interfaces");
        serviceInstance = Objects.requireNonNull(service);
        final String[] serviceContractNames = contracts.stream().map(Class::getName).toArray(String[]::new);
        registration = context.registerService(serviceContractNames, service, identity);
    }

    @Override
    public void setProperties(final Dictionary<String, ?> properties) {
        registration.setProperties(properties);
    }

    Hashtable<String, ?> dumpProperties() {
        final String[] propertyNames = registration.getReference().getPropertyKeys();
        final Hashtable<String, Object> result = new Hashtable<>(propertyNames.length * 2);
        for (final String propertyName : propertyNames)
            result.put(propertyName, registration.getReference().getProperty(propertyName));
        return result;
    }

    @Override
    public T get() {
        return serviceInstance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceReference<S> getReference() {
        return (ServiceReference<S>) registration.getReference();
    }

    @Override
    public void unregister() {
        close();
    }

    /**
     * Releases all resources associated with this object.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() {
        try {
            registration.unregister();
        } finally {
            serviceInstance = null;
        }
    }

    @Override
    public String toString() {
        return registration.toString();
    }

    boolean isPublished() {
        return serviceInstance != null;
    }
}
