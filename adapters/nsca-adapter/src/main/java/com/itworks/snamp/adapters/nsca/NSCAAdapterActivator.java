package com.itworks.snamp.adapters.nsca;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;

/**
 * Represents activator of Nagios adapter.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class NSCAAdapterActivator extends ResourceAdapterActivator<NSCAAdapter> {
    static final String NAME = NSCAAdapter.NAME;

    private static final class NSCAResourceAdapterFactory implements ResourceAdapterFactory<NSCAAdapter>{


        @Override
        public NSCAAdapter createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) throws Exception {
            return new NSCAAdapter(adapterInstance);
        }
    }

    private static final class NSCAConfigurationProvider extends ConfigurationEntityDescriptionManager<NSCAAdapterConfigurationDescriptor>{

        @Override
        protected NSCAAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new NSCAAdapterConfigurationDescriptor();
        }
    }

    @SpecialUse
    public NSCAAdapterActivator(){
        super(NAME, new NSCAResourceAdapterFactory(),  new NSCAConfigurationProvider());
    }
}
