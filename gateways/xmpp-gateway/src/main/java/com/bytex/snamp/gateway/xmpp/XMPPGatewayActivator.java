package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.gateway.GatewayActivator;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class XMPPGatewayActivator extends GatewayActivator<XMPPGateway> {
    public XMPPGatewayActivator() {
        super(XMPPGatewayActivator::newGateway, configurationDescriptor(XMPPGatewayConfigurationProvider::new));
    }

    private static XMPPGateway newGateway(final String instanceName, final DependencyManager dependencies) {
        return new XMPPGateway(instanceName);
    }
}
