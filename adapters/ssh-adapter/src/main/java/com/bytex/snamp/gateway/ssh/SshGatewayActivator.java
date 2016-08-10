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

    @SpecialUse
    public SshGatewayActivator() {
        super(SshGatewayActivator::newGateway, configurationDescriptor(SshGatewayDescriptionProvider::getInstance));
    }

    private static SshGateway newGateway(final String instanceName, final RequiredService<?>... dependencies) {
        return new SshGateway(instanceName);
    }
}
