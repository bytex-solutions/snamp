package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractServiceLibrary;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * The type Web console activator.
 */
public final class WebConsoleActivator extends AbstractServiceLibrary {
    private static final class WebConsoleServletProvider extends ProvidedService<WebConsoleEngine, WebConsoleServlet>{
        private WebConsoleServletProvider(){
            super(WebConsoleEngine.class, simpleDependencies(HttpService.class));
        }

        @Override
        protected WebConsoleServlet activateService(final Map<String, Object> identity) throws InvalidSyntaxException, ServletException, NamespaceException {
            final HttpService httpService = getDependencies().getDependency(HttpService.class);
            assert httpService != null;
            final WebConsoleServlet registry = new WebConsoleServlet();
            httpService.registerServlet(WebConsoleServlet.CONTEXT, registry, new Hashtable<>(), null);
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
                httpService.unregister(WebConsoleServlet.CONTEXT);
        }
    }

    @SpecialUse
    public WebConsoleActivator(){
        super(new WebConsoleServletProvider());
    }

    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
    }

    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) throws Exception {
    }

    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) throws Exception {

    }

    @Override
    protected void shutdown() {

    }
}