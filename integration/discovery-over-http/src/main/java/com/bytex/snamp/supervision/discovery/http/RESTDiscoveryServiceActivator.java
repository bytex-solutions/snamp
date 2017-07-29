package com.bytex.snamp.supervision.discovery.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractServiceLibrary;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;

/**
 * Publishes {@link RESTDiscoveryService} into Servlet container.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class RESTDiscoveryServiceActivator extends AbstractServiceLibrary {
    private static final class DiscoveryServiceProvider extends ProvidedService<Servlet, DiscoveryServiceServlet>{
        private DiscoveryServiceProvider(){
            super(Servlet.class);
        }

        @Override
        @Nonnull
        protected DiscoveryServiceServlet activateService(final ServiceIdentityBuilder identity) {
            identity.setServletContext(DiscoveryServiceServlet.CONTEXT);
            return new DiscoveryServiceServlet();
        }

        @Override
        protected void cleanupService(final DiscoveryServiceServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public RESTDiscoveryServiceActivator(){
        super(new DiscoveryServiceProvider());
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {

    }
}
