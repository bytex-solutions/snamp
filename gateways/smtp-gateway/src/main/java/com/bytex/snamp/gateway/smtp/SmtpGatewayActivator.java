package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;

/**
 * Represents activator of {@link SmtpGateway}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class SmtpGatewayActivator extends GatewayActivator<SmtpGateway> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public SmtpGatewayActivator(){
        super(SmtpGatewayActivator::createGateway, configurationDescriptor(SmtpGatewayConfigurationDescriptionProvider::getInstance));
    }

    private static SmtpGateway createGateway(final String instanceName,
                                             final DependencyManager dependencies){
        return new SmtpGateway(instanceName);
    }
}
