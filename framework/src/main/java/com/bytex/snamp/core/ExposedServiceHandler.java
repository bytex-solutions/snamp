package com.bytex.snamp.core;

import com.bytex.snamp.internal.Utils;
import org.osgi.framework.*;

import java.util.Objects;
import java.util.function.Predicate;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents a handler for OSGi service exposed by the specified bundle.
 * @param <S> Type of the OSGi service.
 * @param <I> Type of the user data to be passed into the handler.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class ExposedServiceHandler<S, I, E extends Throwable> {
    private final Class<S> serviceType;
    private final Predicate<ServiceReference<?>> filter;

    protected ExposedServiceHandler(final Class<S> serviceType,
                          final String filter) throws InvalidSyntaxException {
        this(serviceType, isNullOrEmpty(filter) ? ref -> true : FrameworkUtil.createFilter(filter)::match);
    }

    protected ExposedServiceHandler(final Class<S> serviceType){
        this(serviceType, ref -> true);
    }

    protected ExposedServiceHandler(final Class<S> serviceType, final Predicate<ServiceReference<?>> filter){
        this.filter = Objects.requireNonNull(filter);
        this.serviceType = Objects.requireNonNull(serviceType);
    }

    /**
     * Gets context of the bundle with expected services.
     * @return The bundle context.
     */
    protected BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    /**
     * Handles a service instance exposed by the bundle referenced by {@link #getBundleContext()}.
     * @param service An instance of the service.
     * @param userData Additional user data.
     * @return {@literal true} to process the next exposed service; otherwise, {@literal false}.
     */
    protected abstract boolean handleService(final S service, final I userData) throws E;

    /**
     * Handles a service exposed by the bundle referenced by {@link #getBundleContext()}.
     * @param userData Any object to be passed into processing procedure.
     */
    public final void handleService(final I userData) throws E {
        final BundleContext context = getBundleContext();
        if (context == null) throw new IllegalStateException("Unknown bundle context");
        final ServiceReference<?>[] services = context.getBundle().getRegisteredServices();
        if (services != null)
            for (final ServiceReference<?> ref : services)
                if (filter.test(ref) && Utils.isInstanceOf(ref, serviceType))
                    try {
                        if(!handleService(serviceType.cast(context.getService(ref)), userData))
                            return;
                    } finally {
                        context.ungetService(ref);
                    }
    }
}
