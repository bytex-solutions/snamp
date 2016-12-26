package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractServiceLibrary;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * The type Web console activator.
 */
public final class WebConsoleActivator extends AbstractServiceLibrary {
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    private static final class WebConsoleServletProvider extends ProvidedService<Servlet, WebConsoleServlet>{
        private WebConsoleServletProvider(){
            super(Servlet.class, simpleDependencies(HttpService.class));
        }

        @Override
        protected WebConsoleServlet activateService(final Map<String, Object> identity) throws InvalidSyntaxException, ServletException, NamespaceException {
            final HttpService httpService = getDependencies().getDependency(HttpService.class);
            assert httpService != null;
            final WebConsoleServlet registry = new WebConsoleServlet();
            httpService.registerServlet(WebConsoleServiceServlet.ROOT_CONTEXT, registry, new Hashtable<>(), null);
            return registry;
        }

        @Override
        protected void activated(final WebConsoleServlet servlet) throws InvalidSyntaxException {
            servlet.discoverServices();
        }

        @Override
        protected void cleanupService(final WebConsoleServlet serviceInstance, final boolean stopBundle) {
            final HttpService httpService = getDependencies().getDependency(HttpService.class);
            if (httpService != null)
                httpService.unregister(WebConsoleServiceServlet.ROOT_CONTEXT);
        }
    }

    @SpecialUse
    public WebConsoleActivator(){
        super(new WebConsoleServletProvider());
    }

    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
    }

    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) throws Exception {
        final HttpService httpService = getDependencies().getDependency(HttpService.class);
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, httpService);
    }

    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) throws Exception {

    }

    @Override
    protected void shutdown() {

    }
}