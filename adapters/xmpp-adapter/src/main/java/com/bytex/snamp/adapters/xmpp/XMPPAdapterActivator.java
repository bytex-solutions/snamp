package com.bytex.snamp.adapters.xmpp;

import com.bytex.snamp.adapters.ResourceAdapterActivator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class XMPPAdapterActivator extends ResourceAdapterActivator<XMPPAdapter> {
    private static final class XMPPAdapterFactory implements ResourceAdapterFactory<XMPPAdapter>{

        @Override
        public XMPPAdapter createAdapter(final String adapterInstance, final RequiredService<?>... dependencies) {
            return new XMPPAdapter(adapterInstance);
        }
    }

    private static final class XMPPConfigurationProvider extends ConfigurationEntityDescriptionManager<XMPPAdapterConfigurationProvider>{

        @Override
        protected XMPPAdapterConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new XMPPAdapterConfigurationProvider();
        }
    }

    public XMPPAdapterActivator(){
        super(new XMPPAdapterFactory(), new XMPPConfigurationProvider());
    }
}
