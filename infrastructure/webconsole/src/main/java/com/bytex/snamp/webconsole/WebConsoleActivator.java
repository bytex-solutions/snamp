package com.bytex.snamp.webconsole;

import com.bytex.snamp.core.AbstractBundleActivator;
import static com.bytex.snamp.internal.Utils.acceptWithContextClassLoader;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpService;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

public final class WebConsoleActivator extends AbstractBundleActivator {
    private static final String SERVLET_CONTEXT = "/snamp/webapi";

    private WebConsoleService consoleAPI;
    private ManagementService managementAPI;
    private HttpService publisher;

    @Override
    protected void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
    }

    private static Dictionary<String, String> getServletInitParams(){
        final Dictionary<String, String> params = new Hashtable<>();
        params.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        return params;
    }

    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        @SuppressWarnings("unchecked")
        final ConfigurationAdmin configAdmin = getDependency(RequiredServiceAccessor.class, ConfigurationAdmin.class, dependencies);
        consoleAPI = new WebConsoleService(configAdmin);
        managementAPI = new ManagementService(configAdmin);
        @SuppressWarnings("unchecked")
        final HttpService httpService = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
        acceptWithContextClassLoader(getClass().getClassLoader(),
                httpService,
                (publisher) -> publisher.registerServlet(SERVLET_CONTEXT, new JerseyServletContainer(consoleAPI, managementAPI), getServletInitParams(), null));
        publisher = httpService;
    }

    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {
        publisher.unregister(SERVLET_CONTEXT);
        consoleAPI.close();
    }

    @Override
    protected void shutdown(final BundleContext context) throws Exception {
        publisher = null;
        consoleAPI = null;
    }
}