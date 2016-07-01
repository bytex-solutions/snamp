package com.bytex.snamp.adapters.nrdp;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.SpecialUse;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class NRDPAdapterActivator extends ResourceAdapterActivator<NRDPAdapter> {
    private static final class NRDPResourceAdapterFactory implements ResourceAdapterFactory<NRDPAdapter>{


        @Override
        public NRDPAdapter createAdapter(final String adapterInstance,
                                         final RequiredService<?>... dependencies) {
            return new NRDPAdapter(adapterInstance);
        }
    }

    private static final class NRDPConfigurationProvider extends ConfigurationEntityDescriptionManager<NRDPAdapterConfigurationDescriptor>{

        @Override
        protected NRDPAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return NRDPAdapterConfigurationDescriptor.getInstance();
        }
    }

    @SpecialUse
    public NRDPAdapterActivator(){
        super(new NRDPResourceAdapterFactory(),  new NRDPConfigurationProvider());
    }

}
