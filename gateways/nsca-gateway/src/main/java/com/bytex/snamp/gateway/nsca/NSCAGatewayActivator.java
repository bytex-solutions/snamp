package com.bytex.snamp.gateway.nsca;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;

/**
 * Represents activator of Nagios gateway.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public final class NSCAGatewayActivator extends GatewayActivator<NSCAGateway> {

    @SpecialUse
    public NSCAGatewayActivator(){
        super(NSCAGatewayActivator::newGateway, configurationDescriptor(NSCAGatewayConfigurationDescriptor::getInstance));
    }

    private static NSCAGateway newGateway(final String instanceName,
                                          final RequiredService<?>... dependencies) {
        return new NSCAGateway(instanceName);
    }
}
