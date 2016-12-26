package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.gateway.GatewayActivator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class XMPPGatewayActivator extends GatewayActivator<XMPPGateway> {
    public XMPPGatewayActivator() {
        super(XMPPGatewayActivator::newGateway,
                simpleDependencies(ThreadPoolRepository.class),
                new SupportGatewayServiceManager<?, ?>[]{configurationDescriptor(XMPPGatewayConfigurationProvider::new)});
    }

    private static XMPPGateway newGateway(final String instanceName, final DependencyManager dependencies) {
        final ThreadPoolRepository threadPools = dependencies.getDependency(ThreadPoolRepository.class);
        assert threadPools != null;
        return new XMPPGateway(instanceName, threadPools.getThreadPool("XMPPThreadPool", true));
    }
}
