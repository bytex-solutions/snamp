package com.bytex.snamp.connector.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;

/**
 * Represents activator of {@link HttpAcceptor}.
 */
public final class HttpAcceptorActivator extends ManagedResourceActivator<HttpAcceptor> {

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
        super(HttpAcceptorActivator::newResourceConnector,
                configurationDescriptor(HttpConnectorConfigurationDescriptionProvider::getInstance),
                new HttpAcceptorServletProvider());
    }

    private static HttpAcceptor newResourceConnector(final String resourceName,
                                                     final ManagedResourceInfo configuration,
                                              final DependencyManager dependencies) {
        return new HttpAcceptor(resourceName, configuration);
    }
}
