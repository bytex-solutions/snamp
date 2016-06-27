package com.bytex.snamp.adapters.nsca;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.adapters.nsca.configuration.NSCAAdapterConfigurationDescriptor;

/**
 * Represents activator of Nagios adapter.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public final class NSCAAdapterActivator extends ResourceAdapterActivator<NSCAAdapter> {
    private static final class NSCAResourceAdapterFactory implements ResourceAdapterFactory<NSCAAdapter>{


        @Override
        public NSCAAdapter createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) {
            return new NSCAAdapter(adapterInstance);
        }
    }

    private static final class NSCAConfigurationProvider extends ConfigurationEntityDescriptionManager<NSCAAdapterConfigurationDescriptor>{

        @Override
        protected NSCAAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return NSCAAdapterConfigurationDescriptor.getInstance();
        }
    }

    @SpecialUse
    public NSCAAdapterActivator(){
        super(new NSCAResourceAdapterFactory(),  new NSCAConfigurationProvider());
    }
}
