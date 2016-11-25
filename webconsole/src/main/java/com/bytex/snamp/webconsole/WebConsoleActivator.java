package com.bytex.snamp.webconsole;

import com.bytex.snamp.core.AbstractBundleActivator;
import com.bytex.snamp.management.rest.GatewayService;
import com.bytex.snamp.management.rest.ManagementService;
import com.bytex.snamp.management.rest.ResourceGroupService;
import com.bytex.snamp.management.rest.ResourceService;
import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpService;

import java.util.Collection;
import java.util.Hashtable;

import static com.bytex.snamp.internal.Utils.acceptWithContextClassLoader;

/**
 * The type Web console activator.
 */
public final class WebConsoleActivator extends AbstractBundleActivator {
    private static final String STATIC_SERVLET_CONTEXT = "/snamp/*";

    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    @Override
    protected void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
    }

    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        @SuppressWarnings("unchecked")
        // For Dashboard purpose // temporarily commented out
        // final ConfigurationAdmin configAdmin = getDependency(RequiredServiceAccessor.class, ConfigurationAdmin.class, dependencies);
        final WebConsoleService consoleAPI = new WebConsoleService(getLogger());
        final String resourceBase = this.getClass().getClassLoader().getResource("webapp").toExternalForm();
        @SuppressWarnings("unchecked")
        final HttpService httpService = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
        acceptWithContextClassLoader(getClass().getClassLoader(),
                httpService,
                (publisher) -> {
                    publisher.registerServlet(WebConsoleServlet.CONTEXT,
                            new WebConsoleServlet(consoleAPI, new ResourceService(),
                                    new GatewayService(), new ResourceGroupService(), new ManagementService()), new Hashtable<>(), null);
                    publisher.registerServlet(STATIC_SERVLET_CONTEXT, new DefaultServlet(),
                            new Hashtable<>(ImmutableMap
                                    .of("resourceBase", resourceBase, "pathInfoOnly", "true")
                            ), null);
                });
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, httpService);
    }

    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY).unregister(WebConsoleServlet.CONTEXT);
    }

    @Override
    protected void shutdown(final BundleContext context) {

    }
}