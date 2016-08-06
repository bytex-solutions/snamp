package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import org.osgi.service.event.EventAdmin;
import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents activator of Decanter Adapter.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public final class DecanterAdapterActivator extends ResourceAdapterActivator<DecanterAdapter> {
    private static final class DecanterAdapterFactory implements ResourceAdapterFactory<DecanterAdapter> {

        @SuppressWarnings("unchecked")
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
                emptyArray(SupportAdapterServiceManager[].class)
        );
    }
}
