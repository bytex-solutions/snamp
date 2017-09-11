package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.ReplicationSupport;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;

/**
 * Collects spans compatible with Twitter Zipkin.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
public final class ZipkinConnectorActivator extends ManagedResourceActivator<ZipkinConnector> {
    private static final class ZipkinServletProvider extends SupportServiceManager<ZipkinServlet>{
        private ZipkinServletProvider(){
            super(ServletSupportService.class, Servlet.class);
        }

        @Nonnull
        @Override
        protected ZipkinServlet createService(final ServiceIdentityBuilder identity) {
            identity.setServletContext(ZipkinServlet.CONTEXT);
            return new ZipkinServlet();
        }

        @Override
        protected void cleanupService(final ZipkinServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    private static final class ZipkinConnectorFactory implements ManagedResourceConnectorFactory<ZipkinConnector>{
        @Nonnull
        @Override
        public ZipkinConnector createConnector(final String resourceName, final ManagedResourceInfo configuration, final DependencyManager dependencies) throws Exception {
            return new ZipkinConnector(resourceName, configuration);
        }

        @Override
        public ImmutableSet<Class<? super ZipkinConnector>> getInterfaces() {
            return ImmutableSet.of(ReplicationSupport.class);
        }
    }


    @SpecialUse(SpecialUse.Case.OSGi)
    public ZipkinConnectorActivator(){
        super(new ZipkinConnectorFactory(),
                configurationDescriptor(ZipkinConnectorConfigurationDescriptionProvider::getInstance),
                new ZipkinServletProvider());
    }
}
