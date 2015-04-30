package com.itworks.snamp.adapters.nrdp;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NRDPAdapterActivator extends ResourceAdapterActivator<NRDPAdapter> {
    static final String NAME = NRDPAdapter.NAME;

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
            return new NRDPAdapterConfigurationDescriptor();
        }
    }

    @SpecialUse
    public NRDPAdapterActivator(){
        super(NAME, new NRDPResourceAdapterFactory(),  new NRDPConfigurationProvider());
    }

}