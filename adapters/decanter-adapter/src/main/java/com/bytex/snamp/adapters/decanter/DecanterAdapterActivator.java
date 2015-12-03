package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import org.osgi.service.event.EventAdmin;

/**
 * Represents activator of Decanter Adapter.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class DecanterAdapterActivator extends ResourceAdapterActivator<DecanterAdapter> {
    private static final class DecanterAdapterFactory implements ResourceAdapterFactory<DecanterAdapter> {

        @Override
        public DecanterAdapter createAdapter(final String adapterInstance,
                                             final RequiredService<?>... dependencies) {
            return new DecanterAdapter(adapterInstance, getDependency(RequiredServiceAccessor.class, EventAdmin.class, dependencies));
        }
    }

    @SpecialUse
    public DecanterAdapterActivator() {
        super(new DecanterAdapterFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(EventAdmin.class)},
                new SupportAdapterServiceManager<?, ?>[0]
        );
    }
}
