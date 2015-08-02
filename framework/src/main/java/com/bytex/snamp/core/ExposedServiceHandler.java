package com.bytex.snamp.core;

import com.bytex.snamp.internal.Utils;
import com.google.common.base.Strings;
import org.osgi.framework.*;

import java.util.Objects;

/**
 * Represents a handler for OSGi service exposed by the specified bundle.
 * @param <S> Type of the OSGi service.
 * @param <I> Type of the user data to be passed into the handler.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ExposedServiceHandler<S, I> {
    private final Class<S> serviceType;
    private final Filter filter;

    protected ExposedServiceHandler(final Class<S> serviceType,
                                    final String filter) throws InvalidSyntaxException {
        this.filter = Strings.isNullOrEmpty(filter) ? null : FrameworkUtil.createFilter(filter);
        this.serviceType = Objects.requireNonNull(serviceType);
    }

    /**
     * Gets context of the bundle with expected services.
     * @return The bundle context.
     */
    protected BundleContext getBundleContext(){
        return Utils.getBundleContextByObject(this);
    }

    /**
     * Handles a service instance exposed by the bundle referenced by {@link #getBundleContext()}.
     * @param service An instance of the service.
     * @param userData Additional user data.
     */
    protected abstract void handleService(final S service, final I userData);

    /**
     * Handles a service exposed by the bundle referenced by {@link #getBundleContext()}.
     * @param userData Any object to be passed into processing procedure.
     */
    public final void handleService(final I userData) {
        final BundleContext context = getBundleContext();
        if (context == null) throw new IllegalStateException("Unknown bundle context");
        final ServiceReference<?>[] services = context.getBundle().getRegisteredServices();
        if (services != null)
            for (final ServiceReference<?> ref : services)
                if ((filter == null || filter.match(ref)) && Utils.isInstanceOf(ref, serviceType))
                    try {
                        handleService(serviceType.cast(context.getService(ref)), userData);
                    } finally {
                        context.ungetService(ref);
                    }
    }
}
