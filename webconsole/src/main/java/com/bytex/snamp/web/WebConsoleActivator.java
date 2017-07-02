package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.charts.ChartDataSource;
import com.bytex.snamp.web.serviceModel.commons.ManagedResourceInformationService;
import com.bytex.snamp.web.serviceModel.commons.VersionResource;
import com.bytex.snamp.web.serviceModel.e2e.E2EDataSource;
import com.bytex.snamp.web.serviceModel.logging.LogNotifier;
import com.bytex.snamp.web.serviceModel.logging.LogNotifierController;
import com.bytex.snamp.web.serviceModel.notifications.NotificationService;
import com.bytex.snamp.web.serviceModel.resourceGroups.ResourceGroupWatcherService;
import org.ops4j.pax.logging.PaxLoggingService;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
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
            super(WebConsoleEngine.class, noRequiredServices(), Servlet.class);
        }

        @Override
        @Nonnull
        protected WebConsoleEngineImpl activateService(final Map<String, Object> identity) throws InvalidSyntaxException {
            final WebConsoleEngineImpl registry = new WebConsoleEngineImpl(ClusterMember.get(Utils.getBundleContextOfObject(this)));
            identity.put("alias", WebConsoleEngineImpl.CONTEXT);
            return registry;
        }
    }

    private static abstract class WebConsoleServiceProvider<S extends WebConsoleService, T extends S> extends ProvidedService<S, T>{
        private static final String SERVICE_NAME_PROPERTY = "webConsoleServiceName";
        private final String serviceName;

        WebConsoleServiceProvider(@Nonnull final Class<S> mainContract,
                                  @Nonnull final String serviceName,
                                  final RequiredService<?>... dependencies) {
            super(mainContract, dependencies, WebConsoleService.class);
            this.serviceName = serviceName;
        }

        WebConsoleServiceProvider(@Nonnull final Class<S> mainContract,
                                  @Nonnull final String serviceName,
                                  final RequiredService<?>[] dependencies,
                                  final Class<? super S> subContract) {
            super(mainContract, dependencies, WebConsoleService.class, subContract);
            this.serviceName = serviceName;
        }

        @Nonnull
        abstract T activateService() throws Exception;

        @OverridingMethodsMustInvokeSuper
        void fillIdentity(final Map<String, Object> identity){
            identity.put(SERVICE_NAME_PROPERTY, serviceName);
        }

        @Nonnull
        @Override
        protected final T activateService(final Map<String, Object> identity) throws Exception {
            fillIdentity(identity);
            return activateService();
        }

        @Override
        protected final void cleanupService(final T serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    private static final class WebConsoleServiceDependency extends RequiredServiceAccessor<RESTController>{
        private final String name;

        WebConsoleServiceDependency(@Nonnull final String serviceName) {
            super(RESTController.class);
            name = serviceName;
        }

        @Override
        protected boolean match(final ServiceReference<RESTController> reference) {
            return name.equals(reference.getProperty(WebConsoleServiceProvider.SERVICE_NAME_PROPERTY));
        }
    }

    private static final class WebConsoleServiceServletProvider extends ProvidedService<Servlet, WebConsoleServiceServlet> {
        private static final String ROOT_CONTEXT = "/snamp/web/api";

        WebConsoleServiceServletProvider(@Nonnull final String serviceName) {
            super(Servlet.class, new WebConsoleServiceDependency(serviceName));
        }

        @Nonnull
        @Override
        protected WebConsoleServiceServlet activateService(final Map<String, Object> identity) {
            final RESTController serviceImpl = dependencies.getDependency(WebConsoleServiceDependency.class)
                    .flatMap(WebConsoleServiceDependency::getService)
                    .orElseThrow(AssertionError::new);
            identity.put("alias", ROOT_CONTEXT + serviceImpl.getUrlContext());
            return new WebConsoleServiceServlet(serviceImpl);
        }

        @Override
        protected void cleanupService(final WebConsoleServiceServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    //=============Predefined services for WebConsole======
    private static final class NotificationServiceProvider extends WebConsoleServiceProvider<RESTController, NotificationService>{
        private static final String SERVICE_NAME = "notifications";

        private NotificationServiceProvider() {
            super(RESTController.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        NotificationService activateService() {
            return new NotificationService();
        }
    }

    private static final class GroupWatcherServiceProvider extends WebConsoleServiceProvider<RESTController, ResourceGroupWatcherService>{
        private static final String SERVICE_NAME = "resourceGroupWatcher";

        private GroupWatcherServiceProvider(){
            super(RESTController.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        ResourceGroupWatcherService activateService() {
            return new ResourceGroupWatcherService();
        }
    }

    private static final class E2EDataSourceProvider extends WebConsoleServiceProvider<RESTController, E2EDataSource> {
        private static final String SERVICE_NAME = "E2E";

        private E2EDataSourceProvider() {
            super(RESTController.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        E2EDataSource activateService() throws IOException {
            return new E2EDataSource();
        }
    }

    private static final class ChartDataSourceProvider extends WebConsoleServiceProvider<RESTController, ChartDataSource>{
        private static final String SERVICE_NAME = "chartDataProvider";

        private ChartDataSourceProvider(){
            super(RESTController.class, SERVICE_NAME, requiredBy(ChartDataSource.class).require(ThreadPoolRepository.class));
        }

        @Nonnull
        @Override
        ChartDataSource activateService() {
            final ThreadPoolRepository repository = dependencies.getService(ThreadPoolRepository.class).orElseThrow(AssertionError::new);
            return new ChartDataSource(repository.getThreadPool(THREAD_POOL_NAME, true));
        }
    }

    private static final class ManagedResourceInformationServiceProvider extends WebConsoleServiceProvider<RESTController, ManagedResourceInformationService>{
        private static final String SERVICE_NAME = "managedResourceInformationProvider";

        private ManagedResourceInformationServiceProvider(){
            super(RESTController.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        ManagedResourceInformationService activateService() throws Exception {
            return new ManagedResourceInformationService();
        }
    }

    private static final class VersionResourceProvider extends WebConsoleServiceProvider<RESTController, VersionResource>{
        private static final String SERVICE_NAME = "versionInformation";

        private VersionResourceProvider(){
            super(RESTController.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        VersionResource activateService() {
            return new VersionResource();
        }
    }

    private static final class LogNotifierProvider extends WebConsoleServiceProvider<LogNotifierController, LogNotifier> {
        private static final String SERVICE_NAME = "logNotifier";

        private LogNotifierProvider() {
            super(LogNotifierController.class,
                    SERVICE_NAME,
                    requiredBy(LogNotifier.class).require(ThreadPoolRepository.class),
                    PaxAppender.class);
        }

        @Override
        void fillIdentity(final Map<String, Object> identity) {
            identity.put(PaxLoggingService.APPENDER_NAME_PROPERTY, "SnampWebConsoleLogAppender");
            super.fillIdentity(identity);
        }

        @Nonnull
        @Override
        LogNotifier activateService() throws Exception {
            final ThreadPoolRepository repository = dependencies.getService(ThreadPoolRepository.class).orElseThrow(AssertionError::new);
            return new LogNotifier(repository.getThreadPool(THREAD_POOL_NAME, true));
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public WebConsoleActivator() {
        super(new WebConsoleServletProvider(),
                new LogNotifierProvider(),
                new WebConsoleServiceServletProvider(LogNotifierProvider.SERVICE_NAME),

                new VersionResourceProvider(),
                new WebConsoleServiceServletProvider(VersionResourceProvider.SERVICE_NAME),

                new ManagedResourceInformationServiceProvider(),
                new WebConsoleServiceServletProvider(ManagedResourceInformationServiceProvider.SERVICE_NAME),

                new ChartDataSourceProvider(),
                new WebConsoleServiceServletProvider(ChartDataSourceProvider.SERVICE_NAME),

                new E2EDataSourceProvider(),
                new WebConsoleServiceServletProvider(E2EDataSourceProvider.SERVICE_NAME),

                new GroupWatcherServiceProvider(),
                new WebConsoleServiceServletProvider(GroupWatcherServiceProvider.SERVICE_NAME),

                new NotificationServiceProvider(),
                new WebConsoleServiceServletProvider(NotificationServiceProvider.SERVICE_NAME));
    }

    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) {
    }
}