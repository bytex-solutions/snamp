package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.SpecialUse;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SysLogGatewayActivator extends GatewayActivator<SysLogGateway> {
    @SpecialUse
    public SysLogGatewayActivator(){
        super(SysLogGatewayActivator::newGateway, configurationDescriptor(SysLogConfigurationDescriptor::new));
    }

    private static SysLogGateway newGateway(final String instanceName,
                                            final RequiredService<?>... dependencies) {
        return new SysLogGateway(instanceName);
    }
}
