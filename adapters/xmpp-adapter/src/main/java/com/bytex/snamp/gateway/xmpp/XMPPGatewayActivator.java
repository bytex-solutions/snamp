package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.gateway.GatewayActivator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class XMPPGatewayActivator extends GatewayActivator<XMPPGateway> {
    private static final class XMPPAdapterFactory implements ResourceAdapterFactory<XMPPGateway>{

        @Override
        public XMPPGateway createAdapter(final String adapterInstance, final RequiredService<?>... dependencies) {
            return new XMPPGateway(adapterInstance);
        }
    }

    private static final class XMPPConfigurationProvider extends ConfigurationEntityDescriptionManager<XMPPGatewayConfigurationProvider>{

        @Override
        protected XMPPGatewayConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new XMPPGatewayConfigurationProvider();
        }
    }

    public XMPPGatewayActivator(){
        super(new XMPPAdapterFactory(), new XMPPConfigurationProvider());
    }
}
