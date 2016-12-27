package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.logging.LogNotifier;
import com.bytex.snamp.web.serviceModel.logging.WebConsoleLogService;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.http.HttpService;

import javax.servlet.Servlet;
import java.util.Collection;
import java.util.Map;

/**
 * The type Web console activator.
 */
public final class WebConsoleActivator extends AbstractServiceLibrary {
    private static final class WebConsoleServletProvider extends ProvidedService<WebConsoleEngine, WebConsoleEngineImpl>{
        private WebConsoleServletProvider(){
            super(WebConsoleEngine.class, simpleDependencies(), Servlet.class);
        }

        @Override
        protected WebConsoleEngineImpl activateService(final Map<String, Object> identity) throws InvalidSyntaxException {
            final WebConsoleEngineImpl registry = new WebConsoleEngineImpl();
            identity.put("alias", WebConsoleEngineImpl.CONTEXT);
            return registry;
        }

        @Override
        protected void cleanupService(final WebConsoleEngineImpl serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    //=============Predefined services for WebConsole======
    private static final class LogNotifierProvider extends ProvidedService<WebConsoleLogService, LogNotifier> {
        private LogNotifierProvider() {
            super(WebConsoleLogService.class, simpleDependencies(WebConsoleEngine.class), WebConsoleService.class, PaxAppender.class);
        }

        @Override
        protected LogNotifier activateService(final Map<String, Object> identity) {
            identity.put(WebConsoleService.NAME, LogNotifier.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, "/logging");
            identity.put(PaxLoggingService.APPENDER_NAME_PROPERTY, "SnampWebConsoleLogAppender");
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
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
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