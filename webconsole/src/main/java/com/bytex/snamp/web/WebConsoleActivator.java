package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.logging.LogNotifier;
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
    private static final class WebConsoleServletProvider extends ProvidedService<WebConsoleEngine, WebConsoleEngineImpl>{
        private WebConsoleServletProvider(){
            super(WebConsoleEngine.class, simpleDependencies(HttpService.class));
        }

        @Override
        protected WebConsoleEngineImpl activateService(final Map<String, Object> identity) throws InvalidSyntaxException, ServletException, NamespaceException {
            final HttpService httpService = getDependencies().getDependency(HttpService.class);
            assert httpService != null;
            final WebConsoleEngineImpl registry = new WebConsoleEngineImpl();
            httpService.registerServlet(WebConsoleEngineImpl.CONTEXT, registry, new Hashtable<>(), null);
            return registry;
        }

        @Override
        protected void activated(final WebConsoleEngineImpl servlet) throws InvalidSyntaxException {
            servlet.discoverServices();
        }

        @Override
        protected void cleanupService(final WebConsoleEngineImpl serviceInstance, final boolean stopBundle) {
            final HttpService httpService = getDependencies().getDependency(HttpService.class);
            if (httpService != null)
                httpService.unregister(WebConsoleEngineImpl.CONTEXT);
        }
    }

    //=============Predefined services for WebConsole======
    private static final class LogNotifierProvider extends ProvidedService<WebConsoleService, LogNotifier>{
        private LogNotifierProvider(){
            super(WebConsoleService.class, simpleDependencies(WebConsoleEngine.class));
        }

        @Override
        protected LogNotifier activateService(final Map<String, Object> identity) {
            identity.put(WebConsoleService.NAME, LogNotifier.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, "/logging");
            return new LogNotifier();
        }

        @Override
        protected void cleanupService(final LogNotifier serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    @SpecialUse
    public WebConsoleActivator(){
        super(new WebConsoleServletProvider(), new LogNotifierProvider());
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