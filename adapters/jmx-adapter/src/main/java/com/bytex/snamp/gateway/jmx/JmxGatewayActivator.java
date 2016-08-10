package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * Represents JMX resource adapter activator.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JmxGatewayActivator extends GatewayActivator<JmxGateway> {
    private static final class JmxConfigurationDescriptor extends ConfigurationEntityDescriptionManager<JmxGatewayConfigurationProvider> {

        @Override
        protected JmxGatewayConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) throws Exception {
            return new JmxGatewayConfigurationProvider();
        }
    }

    private static final class JmxAdapterFactory implements ResourceAdapterFactory<JmxGateway>{

        @Override
        public JmxGateway createAdapter(final String adapterInstance,
                                        final RequiredService<?>... dependencies) throws Exception {
            return new JmxGateway(adapterInstance);
        }
    }

    /**
     * Initializes a new instance of the resource adapter lifetime manager.
     */
    @SpecialUse
    public JmxGatewayActivator() {
        super(new JmxAdapterFactory(),
                new JmxConfigurationDescriptor());
    }
}
