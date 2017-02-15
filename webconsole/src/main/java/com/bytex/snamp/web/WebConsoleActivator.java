package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.charts.ChartDataSource;
import com.bytex.snamp.web.serviceModel.commons.VersionResource;
import com.bytex.snamp.web.serviceModel.e2e.E2EDataSource;
import com.bytex.snamp.web.serviceModel.logging.LogNotifier;
import com.bytex.snamp.web.serviceModel.commons.ManagedResourceInformationService;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.http.HttpService;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * The type Web console activator.
 */
public final class WebConsoleActivator extends AbstractServiceLibrary {
    private static final String THREAD_POOL_NAME = "WebConsoleThreadPool";

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
    private static final class E2EDataSourceProvider extends ProvidedService<WebConsoleService, E2EDataSource>{
        private E2EDataSourceProvider(){
            super(WebConsoleService.class, simpleDependencies(TopologyAnalyzer.class));
        }

        @Override
        protected E2EDataSource activateService(final Map<String, Object> identity) throws IOException {
            identity.put(WebConsoleService.NAME, E2EDataSource.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, E2EDataSource.URL_CONTEXT);
            return new E2EDataSource(getDependencies().getDependency(TopologyAnalyzer.class));
        }

        @Override
        protected void cleanupService(final E2EDataSource serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    private static final class ChartDataSourceProvider extends ProvidedService<WebConsoleService, ChartDataSource>{
        private ChartDataSourceProvider(){
            super(WebConsoleService.class, simpleDependencies(ConfigurationManager.class, ThreadPoolRepository.class));
        }

        @Override
        protected ChartDataSource activateService(final Map<String, Object> identity) throws IOException {
            identity.put(WebConsoleService.NAME, ChartDataSource.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, ChartDataSource.URL_CONTEXT);
            final ThreadPoolRepository repository = getDependencies().getDependency(ThreadPoolRepository.class);
            assert repository != null;
            return new ChartDataSource(getDependencies().getDependency(ConfigurationManager.class), repository.getThreadPool(THREAD_POOL_NAME, true));
        }

        @Override
        protected void cleanupService(final ChartDataSource serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    private static final class ManagedResourceInformationServiceProvider extends ProvidedService<WebConsoleService, ManagedResourceInformationService>{
        private ManagedResourceInformationServiceProvider(){
            super(WebConsoleService.class, simpleDependencies(WebConsoleEngine.class));
        }

        @Override
        protected ManagedResourceInformationService activateService(final Map<String, Object> identity) {
            identity.put(WebConsoleService.NAME, ManagedResourceInformationService.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, ManagedResourceInformationService.URL_CONTEXT);
            return new ManagedResourceInformationService();
        }

        @Override
        protected void cleanupService(final ManagedResourceInformationService serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    private static final class VersionResourceProvider extends ProvidedService<WebConsoleService, VersionResource>{
        private VersionResourceProvider(){
            super(WebConsoleService.class, simpleDependencies(WebConsoleEngine.class));
        }

        @Override
        protected VersionResource activateService(final Map<String, Object> identity) {
            identity.put(WebConsoleService.NAME, VersionResource.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, VersionResource.URL_CONTEXT);
            return new VersionResource();
        }
    }

    private static final class LogNotifierProvider extends ProvidedService<WebConsoleService, LogNotifier> {
        private LogNotifierProvider() {
            super(WebConsoleService.class, simpleDependencies(WebConsoleEngine.class, ThreadPoolRepository.class));
        }

        @Override
        protected LogNotifier activateService(final Map<String, Object> identity) {
            identity.put(WebConsoleService.NAME, LogNotifier.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, LogNotifier.URL_CONTEXT);
            final ThreadPoolRepository repository = getDependencies().getDependency(ThreadPoolRepository.class);
            assert repository != null;
            return new LogNotifier(repository.getThreadPool(THREAD_POOL_NAME, true));
        }

        @Override
        protected void cleanupService(final LogNotifier serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public WebConsoleActivator() {
        super(new WebConsoleServletProvider(),
                new LogNotifierProvider(),
                new VersionResourceProvider(),
                new ManagedResourceInformationServiceProvider(),
                new ChartDataSourceProvider(),
                new E2EDataSourceProvider());
    }

    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
    }

    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) {
    }

    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {

    }

    @Override
    protected void shutdown() {

    }
}