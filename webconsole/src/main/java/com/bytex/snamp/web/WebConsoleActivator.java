package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.supervision.HealthStatusProvider;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.charts.ChartDataSource;
import com.bytex.snamp.web.serviceModel.commons.ManagedResourceInformationService;
import com.bytex.snamp.web.serviceModel.commons.VersionResource;
import com.bytex.snamp.web.serviceModel.e2e.E2EDataSource;
import com.bytex.snamp.web.serviceModel.logging.LogNotifier;
import com.bytex.snamp.web.serviceModel.notifications.NotificationService;
import com.bytex.snamp.web.serviceModel.watcher.GroupWatcherService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.http.HttpService;

import javax.servlet.Servlet;
import java.io.IOException;
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
    private static final class NotificationServiceProvider extends ProvidedService<WebConsoleService, NotificationService>{
        private NotificationServiceProvider(){
            super(WebConsoleService.class, simpleDependencies(ThreadPoolRepository.class));
        }

        @Override
        protected NotificationService activateService(final Map<String, Object> identity) throws Exception {
            identity.put(WebConsoleService.NAME, NotificationService.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, NotificationService.URL_CONTEXT);
            final ThreadPoolRepository repository = dependencies.getDependency(ThreadPoolRepository.class);
            assert repository != null;
            return new NotificationService(repository.getThreadPool(THREAD_POOL_NAME, true));
        }
    }

    private static final class GroupWatcherServiceProvider extends ProvidedService<WebConsoleService, GroupWatcherService>{
        private GroupWatcherServiceProvider(){
            super(WebConsoleService.class, simpleDependencies(HealthStatusProvider.class));
        }

        @Override
        protected GroupWatcherService activateService(final Map<String, Object> identity) {
            identity.put(WebConsoleService.NAME, GroupWatcherService.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, GroupWatcherService.URL_CONTEXT);
            final HealthStatusProvider supervisor = dependencies.getDependency(HealthStatusProvider.class);
            return new GroupWatcherService(supervisor);
        }

        @Override
        protected void cleanupService(final GroupWatcherService serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    private static final class E2EDataSourceProvider extends ProvidedService<WebConsoleService, E2EDataSource>{
        private E2EDataSourceProvider(){
            super(WebConsoleService.class, simpleDependencies(TopologyAnalyzer.class));
        }

        @Override
        protected E2EDataSource activateService(final Map<String, Object> identity) throws IOException {
            identity.put(WebConsoleService.NAME, E2EDataSource.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, E2EDataSource.URL_CONTEXT);
            return new E2EDataSource(dependencies.getDependency(TopologyAnalyzer.class));
        }

        @Override
        protected void cleanupService(final E2EDataSource serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    private static final class ChartDataSourceProvider extends ProvidedService<WebConsoleService, ChartDataSource>{
        private ChartDataSourceProvider(){
            super(WebConsoleService.class);
        }

        @Override
        protected ChartDataSource activateService(final Map<String, Object> identity) {
            identity.put(WebConsoleService.NAME, ChartDataSource.NAME);
            identity.put(WebConsoleService.URL_CONTEXT, ChartDataSource.URL_CONTEXT);
            return new ChartDataSource();
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
            final ThreadPoolRepository repository = dependencies.getDependency(ThreadPoolRepository.class);
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
                new E2EDataSourceProvider(),
                new GroupWatcherServiceProvider(),
                new NotificationServiceProvider());
    }

    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(HttpService.class);
    }
}