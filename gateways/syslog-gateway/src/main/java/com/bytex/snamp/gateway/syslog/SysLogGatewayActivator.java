package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class SysLogGatewayActivator extends GatewayActivator<SysLogGateway> {
    @SpecialUse(SpecialUse.Case.OSGi)
    public SysLogGatewayActivator(){
        super(SysLogGatewayActivator::newGateway, configurationDescriptor(SysLogConfigurationDescriptor::getInstance));
    }

    private static SysLogGateway newGateway(final String instanceName,
                                            final DependencyManager dependencies) {
        return new SysLogGateway(instanceName);
    }
}
