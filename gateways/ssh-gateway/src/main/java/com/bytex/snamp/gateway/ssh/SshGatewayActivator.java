package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;

/**
 * Represents OSGi activator for {@link SshGateway} gateway.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SshGatewayActivator extends GatewayActivator<SshGateway> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public SshGatewayActivator() {
        super(SshGatewayActivator::newGateway, configurationDescriptor(SshGatewayDescriptionProvider::getInstance));
    }

    private static SshGateway newGateway(final String instanceName, final DependencyManager dependencies) {
        return new SshGateway(instanceName);
    }
}
