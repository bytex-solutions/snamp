package com.bytex.snamp.web;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.web.serviceModel.PrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.charts.ChartDataSource;
import com.bytex.snamp.web.serviceModel.commons.ManagedResourceInformationService;
import com.bytex.snamp.web.serviceModel.commons.UserProfileService;
import com.bytex.snamp.web.serviceModel.commons.VersionResource;
import com.bytex.snamp.web.serviceModel.e2e.E2EDataSource;
import com.bytex.snamp.web.serviceModel.logging.LogNotifier;
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

/**
 * The type Web console activator.
 */
public final class WebConsoleActivator extends AbstractServiceLibrary {
    private static final String THREAD_POOL_NAME = "WebConsoleThreadPool";

    private static final class WebConsoleServletProvider extends ProvidedService<DefaultWebConsoleEngine>{
        private WebConsoleServletProvider(){
            super(WebConsoleEngine.class, Servlet.class);
        }

        @Override
        @Nonnull
        protected DefaultWebConsoleEngine activateService(final ServiceIdentityBuilder identity) throws InvalidSyntaxException {
            final DefaultWebConsoleEngine registry = new DefaultWebConsoleEngine(ClusterMember.get(Utils.getBundleContextOfObject(this)));
            identity.setServletContext(DefaultWebConsoleEngine.CONTEXT);
            return registry;
        }
    }

    private static abstract class WebConsoleServiceProvider<T extends WebConsoleService> extends ProvidedService<T>{
        private final String serviceName;

        WebConsoleServiceProvider(@Nonnull final Class<? super T> mainContract,
                                  @Nonnull final String serviceName,
                                  final RequiredService<?>... dependencies) {
            super(mainContract, WebConsoleService.class, dependencies);
            this.serviceName = serviceName;
        }

        WebConsoleServiceProvider(@Nonnull final Class<? super T> mainContract,
                                  @Nonnull final Class<? super T> subContract,
                                  @Nonnull final String serviceName,
                                  final RequiredService<?>... dependencies) {
            super(mainContract, WebConsoleService.class, subContract, dependencies);
            this.serviceName = serviceName;
        }

        WebConsoleServiceProvider(@Nonnull final Class<? super T> mainContract,
                                  @Nonnull final Class<? super T> subContract1,
                                  @Nonnull final Class<? super T> subContract2,
                                  @Nonnull final String serviceName,
                                  final RequiredService<?>... dependencies) {
            super(mainContract, WebConsoleService.class, subContract1, subContract2, dependencies);
            this.serviceName = serviceName;
        }

        @Nonnull
        abstract T activateService();

        @OverridingMethodsMustInvokeSuper
        void fillIdentity(final ServiceIdentityBuilder identity) {
            identity.accept(WebConsoleService.SERVICE_NAME_PROPERTY, serviceName);
        }

        @Nonnull
        @Override
        protected final T activateService(final ServiceIdentityBuilder identity) throws Exception {
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
            return WebConsoleService.getServiceName(reference).map(name::equals).orElse(false);
        }
    }

    private static final class WebConsoleServiceServletProvider extends ProvidedService<WebConsoleServiceServlet> {
        private static final String ROOT_CONTEXT = "/snamp/web/api";

        WebConsoleServiceServletProvider(@Nonnull final String serviceName) {
            super(Servlet.class, new WebConsoleServiceDependency(serviceName));
        }

        @Nonnull
        @Override
        protected WebConsoleServiceServlet activateService(final ServiceIdentityBuilder identity) {
            final RESTController serviceImpl = dependencies.getDependency(WebConsoleServiceDependency.class)
                    .flatMap(WebConsoleServiceDependency::getService)
                    .orElseThrow(AssertionError::new);
            identity.setServletContext(ROOT_CONTEXT + serviceImpl.getUrlContext());
            return new WebConsoleServiceServlet(serviceImpl);
        }

        @Override
        protected void cleanupService(final WebConsoleServiceServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    //=============Predefined services for WebConsole======
    private static final class NotificationServiceProvider extends WebConsoleServiceProvider<NotificationService>{
        private static final String SERVICE_NAME = "notifications";

        private NotificationServiceProvider() {
            super(RESTController.class, PrincipalBoundedService.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        NotificationService activateService() {
            return new NotificationService();
        }
    }

    private static final class GroupWatcherServiceProvider extends WebConsoleServiceProvider<ResourceGroupWatcherService>{
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

    private static final class E2EDataSourceProvider extends WebConsoleServiceProvider<E2EDataSource> {
        private static final String SERVICE_NAME = "E2E";

        private E2EDataSourceProvider() {
            super(RESTController.class, PrincipalBoundedService.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        E2EDataSource activateService() {
            return new E2EDataSource();
        }
    }

    private static final class ChartDataSourceProvider extends WebConsoleServiceProvider<ChartDataSource>{
        private static final String SERVICE_NAME = "chartDataProvider";

        private ChartDataSourceProvider() {
            super(RESTController.class, PrincipalBoundedService.class, SERVICE_NAME, requiredBy(ChartDataSource.class).require(ThreadPoolRepository.class));
        }

        @Nonnull
        @Override
        ChartDataSource activateService() {
            final ThreadPoolRepository repository = dependencies.getService(ThreadPoolRepository.class);
            return new ChartDataSource(repository.getThreadPool(THREAD_POOL_NAME, true));
        }
    }

    private static final class ManagedResourceInformationServiceProvider extends WebConsoleServiceProvider<ManagedResourceInformationService>{
        private static final String SERVICE_NAME = "managedResourceInformationProvider";

        private ManagedResourceInformationServiceProvider(){
            super(RESTController.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        ManagedResourceInformationService activateService() {
            return new ManagedResourceInformationService();
        }
    }

    private static final class VersionResourceProvider extends WebConsoleServiceProvider<VersionResource>{
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

    private static final class LogNotifierProvider extends WebConsoleServiceProvider<LogNotifier> {
        private static final String SERVICE_NAME = "logNotifier";

        private LogNotifierProvider() {
            super(RESTController.class,
                    PaxAppender.class,
                    PrincipalBoundedService.class,
                    SERVICE_NAME,
                    requiredBy(LogNotifier.class).require(ThreadPoolRepository.class));
        }

        @Override
        void fillIdentity(final ServiceIdentityBuilder identity) {
            identity.accept(PaxLoggingService.APPENDER_NAME_PROPERTY, "SnampWebConsoleLogAppender");
            super.fillIdentity(identity);
        }

        @Nonnull
        @Override
        LogNotifier activateService() {
            final ThreadPoolRepository repository = dependencies.getService(ThreadPoolRepository.class);
            return new LogNotifier(repository.getThreadPool(THREAD_POOL_NAME, true));
        }
    }

    private static final class UserProfileServiceProvider extends WebConsoleServiceProvider<UserProfileService>{
        private static final String SERVICE_NAME = "userProfile";

        private UserProfileServiceProvider(){
            super(RESTController.class, SERVICE_NAME);
        }

        @Nonnull
        @Override
        UserProfileService activateService() {
            return new UserProfileService();
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
                new WebConsoleServiceServletProvider(NotificationServiceProvider.SERVICE_NAME),

                new UserProfileServiceProvider(),
                new WebConsoleServiceServletProvider(UserProfileServiceProvider.SERVICE_NAME));
    }

    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) {
    }
}