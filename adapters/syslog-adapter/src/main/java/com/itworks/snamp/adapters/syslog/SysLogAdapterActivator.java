package com.itworks.snamp.adapters.syslog;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SysLogAdapterActivator extends ResourceAdapterActivator<SysLogAdapter> {
    private static final class SysLogAdapterFactory implements ResourceAdapterFactory<SysLogAdapter>{

        @Override
        public SysLogAdapter createAdapter(final String adapterInstance,
                                           final RequiredService<?>... dependencies) {
            return new SysLogAdapter(adapterInstance);
        }
    }

    private static final class SysLogConfigurationProvider extends ConfigurationEntityDescriptionManager<SysLogConfigurationDescriptor>{
        @Override
        protected SysLogConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new SysLogConfigurationDescriptor();
        }
    }

    @SpecialUse
    public SysLogAdapterActivator(){
        super(new SysLogAdapterFactory(), new SysLogConfigurationProvider());
    }
}
