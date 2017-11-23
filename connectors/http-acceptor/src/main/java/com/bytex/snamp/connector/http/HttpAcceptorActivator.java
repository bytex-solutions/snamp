package com.bytex.snamp.connector.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.ReplicationSupport;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;

/**
 * Represents activator of {@link HttpAcceptor}.
 */
public final class HttpAcceptorActivator extends ManagedResourceActivator<HttpAcceptor> {

    private static final class HttpAcceptorFactory implements ManagedResourceConnectorFactory<HttpAcceptor>{

        @Nonnull
        @Override
        public HttpAcceptor createConnector(final String resourceName, final ManagedResourceInfo configuration, final DependencyManager dependencies) throws Exception {
            return new HttpAcceptor(resourceName, configuration);
        }

        @Override
        public ImmutableSet<Class<? super HttpAcceptor>> getInterfaces() {
            return ImmutableSet.of(ReplicationSupport.class);
        }
    }

    private static final class HttpAcceptorServletProvider extends SupportServiceManager<JerseyServletContainer>{
        private HttpAcceptorServletProvider(){
            super(ServletSupportService.class, Servlet.class);
        }

        @Nonnull
        @Override
        protected JerseyServletContainer createService(final ServiceIdentityBuilder identity) {
            identity.setServletContext(JerseyServletContainer.CONTEXT);
            return new JerseyServletContainer();
        }

        @Override
        protected void cleanupService(final JerseyServletContainer servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public HttpAcceptorActivator() {
        super(new HttpAcceptorFactory(),
                configurationDescriptor(HttpConnectorConfigurationDescriptionProvider::getInstance),
                new HttpAcceptorServletProvider());
    }
}
