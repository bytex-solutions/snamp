package com.bytex.snamp.core;

import com.bytex.snamp.Stateful;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents builder of OSGi filter.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface FilterBuilder extends Supplier<Filter>, Stateful {
    default void addServiceListener(final BundleContext context, final ServiceListener listener) {
        final String filter = toString();
        callUnchecked(() -> {
            context.addServiceListener(listener, filter);
            return null;
        });
    }

    default <S> Optional<ServiceReference<S>> getServiceReference(final BundleContext context, final Class<S> serviceType) {
        final String filter = toString();
        final Collection<ServiceReference<S>> refs = callUnchecked(() -> context.getServiceReferences(serviceType, filter));
        return refs.isEmpty() ? Optional.empty() : Optional.of(refs.iterator().next());
    }

    FilterBuilder setServiceType(final Class<?> serviceType);

    /**
     * Constructs OSGi filter in text format.
     * @return OSGi filter in text format.
     */
    @Override
    String toString();
}
