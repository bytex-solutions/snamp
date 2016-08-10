package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * Represents OSGi activator for {@link SshGateway} resource adapter.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SshGatewayActivator extends GatewayActivator<SshGateway> {

    private static final class ConfigurationDescriptorServiceManager extends ConfigurationEntityDescriptionManager<SshGatewayDescriptionProvider> {

        /**
         * Creates a new instance of the configuration description provider.
         *
         * @param dependencies A collection of provider dependencies.
         * @return A new instance of the configuration description provider.
         */
        @Override
        protected SshGatewayDescriptionProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return SshGatewayDescriptionProvider.getInstance();
        }
    }

    private static final class SshAdapterFactory implements ResourceAdapterFactory<SshGateway>{

        @Override
        public SshGateway createAdapter(final String adapterInstance, final RequiredService<?>... dependencies) throws Exception {
            return new SshGateway(adapterInstance);
        }
    }

    @SpecialUse
    public SshGatewayActivator() {
        super(new SshAdapterFactory(),
                new ConfigurationDescriptorServiceManager());
    }
}
