package com.itworks.snamp.adapters.nagios;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.internal.annotations.SpecialUse;
import org.osgi.service.http.HttpService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NagiosAdapterActivator extends ResourceAdapterActivator<NagiosAdapter> {
    private static final class NagiosAdapterFactory implements ResourceAdapterFactory<NagiosAdapter>{

        @Override
        public NagiosAdapter createAdapter(final String adapterInstance,
                                           final RequiredService<?>... dependencies) {
            return new NagiosAdapter(adapterInstance, getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies));
        }
    }

    private static final class NagiosConfigurationProvider extends ConfigurationEntityDescriptionManager<NagiosAdapterConfigurationDescriptor>{

        @Override
        protected NagiosAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new NagiosAdapterConfigurationDescriptor();
        }
    }

    @SpecialUse
    public NagiosAdapterActivator() {
        super(new NagiosAdapterFactory(),
                new RequiredService<?>[]{new SimpleDependency<>(HttpService.class)},
                new SupportAdapterServiceManager<?, ?>[]{new NagiosConfigurationProvider(), new RuntimeInformationServiceManager()});
    }
}
