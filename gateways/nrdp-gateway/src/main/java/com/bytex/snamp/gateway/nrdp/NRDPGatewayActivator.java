package com.bytex.snamp.gateway.nrdp;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NRDPGatewayActivator extends GatewayActivator<NRDPGateway> {

    @SpecialUse
    public NRDPGatewayActivator(){
        super(NRDPGatewayActivator::newGateway, configurationDescriptor(NRDPGatewayConfigurationDescriptor::getInstance));
    }

    private static NRDPGateway newGateway(final String instanceName,
                                          final RequiredService<?>... dependencies){
        return new NRDPGateway(instanceName);
    }
}
