package com.bytex.snamp.core;

import com.bytex.snamp.concurrent.SpinWait;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Objects;

/**
 * Used for waiting of service registration.
 * This class cannot be inherited.
 * @param <S> Type of service contract.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ServiceSpinWait<S> extends SpinWait<ServiceReference<S>> {
    private final BundleContext context;
    private final Class<S> serviceContract;

    public ServiceSpinWait(final BundleContext context, final Class<S> serviceContract){
        this.context = Objects.requireNonNull(context);
        this.serviceContract = Objects.requireNonNull(serviceContract);
    }

    /**
     * Gets an object used as indicator to break the spinning.
     * <p>
     * Spinning will continue until this method return not {@literal null}.
     * </p>
     *
     * @return An object used as indicator to break the spinning.
     */
    @Override
    protected ServiceReference<S> spin() {
        return context.getServiceReference(serviceContract);
    }
}
