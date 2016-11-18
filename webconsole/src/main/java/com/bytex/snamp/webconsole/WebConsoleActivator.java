package com.bytex.snamp.webconsole;

import com.bytex.snamp.core.AbstractBundleActivator;
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
    private static final String API_SERVLET_CONTEXT = "/snamp/console";
    private static final String STATIC_SERVLET_CONTEXT = "/snamp/*";

    private WebConsoleService consoleAPI;
    private ManagementService managementAPI;
    private HttpService publisher;

    @Override
    protected void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
    }

    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        @SuppressWarnings("unchecked")
        final ConfigurationAdmin configAdmin = getDependency(RequiredServiceAccessor.class, ConfigurationAdmin.class, dependencies);
        consoleAPI = new WebConsoleService();
        managementAPI = new ManagementService(configAdmin);
        final String resourceBase = this.getClass().getClassLoader().getResource("webapp").toExternalForm();
        @SuppressWarnings("unchecked")
        final HttpService httpService = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
        acceptWithContextClassLoader(getClass().getClassLoader(),
                httpService,
                (publisher) -> {
                    publisher.registerServlet(API_SERVLET_CONTEXT, new JerseyServletContainer(consoleAPI, managementAPI), new Hashtable<>(), null);
                    publisher.registerServlet(STATIC_SERVLET_CONTEXT, new DefaultServlet(),
                            new Hashtable<>(ImmutableMap
                                    .of("resourceBase", resourceBase, "pathInfoOnly", "true")
                            ), null);
                });
        publisher = httpService;

    }

    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        publisher.unregister(API_SERVLET_CONTEXT);
        consoleAPI.close();
    }

    @Override
    protected void shutdown(final BundleContext context) throws Exception {
        publisher = null;
        consoleAPI = null;
        managementAPI = null;
    }
}