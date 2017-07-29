package com.bytex.snamp.core;

import com.bytex.snamp.Stateful;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Supplier;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.ArrayUtils.getFirst;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents builder of OSGi filter.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface ServiceSelector extends Supplier<Filter>, Stateful {
    default void addServiceListener(final BundleContext context, final ServiceListener listener) {
        final String filter = toString();
        callUnchecked(() -> {
            context.addServiceListener(listener, filter);
            return null;
        });
    }

    default <S> Optional<ServiceReference<S>> getServiceReference(final BundleContext context, final Class<S> serviceType) {
        return getFirst(getServiceReferences(context, serviceType));
    }

    default <S> ServiceReference<S>[] getServiceReferences(final BundleContext context, final Class<S> serviceType) {
        final String filter = toString();
        @SuppressWarnings("unchecked")
        final ServiceReference<S>[] refs = (ServiceReference<S>[]) callUnchecked(() -> {
            final ServiceReference<?>[] result = context.getAllServiceReferences(serviceType.getName(), filter);
            return result == null ? emptyArray(ServiceReference[].class) : result;
        });
        return refs;
    }

    @Nonnull
    ServiceSelector setServiceType(@Nonnull final Class<?> serviceType);

    /**
     * Constructs OSGi filter in text format.
     * @return OSGi filter in text format.
     */
    @Override
    String toString();
}
