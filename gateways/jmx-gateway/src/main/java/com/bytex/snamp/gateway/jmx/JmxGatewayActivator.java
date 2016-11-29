package com.bytex.snamp.gateway.jmx;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;

/**
 * Represents JMX gateway activator.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JmxGatewayActivator extends GatewayActivator<JmxGateway> {

    /**
     * Initializes a new instance of the gateway lifetime manager.
     */
    @SpecialUse
    public JmxGatewayActivator() {
        super(JmxGatewayActivator::newGateway, configurationDescriptor(JmxGatewayConfigurationProvider::new));
    }

    private static JmxGateway newGateway(final String instanceName,
                                         final RequiredService<?>... dependencies) {
        return new JmxGateway(instanceName);
    }
}
