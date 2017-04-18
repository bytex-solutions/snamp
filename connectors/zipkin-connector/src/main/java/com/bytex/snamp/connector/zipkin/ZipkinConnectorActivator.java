package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Collects spans compatible with Twitter Zipkin.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public final class ZipkinConnectorActivator extends ManagedResourceActivator<ZipkinConnector> {
    private static final class ZipkinServletProvider extends SupportServiceManager<ServletSupportService, ZipkinServlet>{
        private ZipkinServletProvider(){
            super(ServletSupportService.class, simpleDependencies(), Servlet.class);
        }

        @Nonnull
        @Override
        protected ZipkinServlet createService(final Map<String, Object> identity) {
            identity.put("alias", ZipkinServlet.CONTEXT);
            return new ZipkinServlet();
        }

        @Override
        protected void cleanupService(final ZipkinServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }


    @SpecialUse(SpecialUse.Case.OSGi)
    public ZipkinConnectorActivator(){
        super(ZipkinConnectorActivator::createConnector,
                configurationDescriptor(ZipkinConnectorConfigurationDescriptionProvider::getInstance),
                new ZipkinServletProvider());
    }

    private static ZipkinConnector createConnector(final String resourceName,
                                                   final ManagedResourceInfo configuration,
                                                   final DependencyManager dependencies) throws URISyntaxException {
        return new ZipkinConnector(resourceName, configuration);
    }
}
