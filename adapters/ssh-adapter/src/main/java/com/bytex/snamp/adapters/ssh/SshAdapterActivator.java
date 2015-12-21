package com.bytex.snamp.adapters.ssh;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.SpecialUse;

/**
 * Represents OSGi activator for {@link com.bytex.snamp.adapters.ssh.SshAdapter} resource adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SshAdapterActivator extends ResourceAdapterActivator<SshAdapter> {

    private static final class ConfigurationDescriptorServiceManager extends ConfigurationEntityDescriptionManager<SshAdapterConfigurationDescriptor> {

        /**
         * Creates a new instance of the configuration description provider.
         *
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         */
        @Override
        protected SshAdapterConfigurationDescriptor createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new SshAdapterConfigurationDescriptor();
        }
    }

    private static final class SshAdapterFactory implements ResourceAdapterFactory<SshAdapter>{

        @Override
        public SshAdapter createAdapter(final String adapterInstance, final RequiredService<?>... dependencies) throws Exception {
            return new SshAdapter(adapterInstance);
        }
    }

    @SpecialUse
    public SshAdapterActivator() {
        super(new SshAdapterFactory(),
                new ConfigurationDescriptorServiceManager());
    }
}
