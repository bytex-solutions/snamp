package com.bytex.snamp.webconsole;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.acceptWithContextClassLoader;

/**
 * The type Web console activator.
 */
public final class WebConsoleActivator extends AbstractServiceLibrary {
    private static final String STATIC_SERVLET_CONTEXT = "/snamp/*";
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    private static final class WebConsoleServletProvider extends ProvidedService<Servlet, WebConsoleServlet>{
        private WebConsoleServletProvider(){
            super(Servlet.class, new SimpleDependency<>(HttpService.class));
        }

        @Override
        protected WebConsoleServlet activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws InvalidSyntaxException, ServletException, NamespaceException {
            @SuppressWarnings("unchecked")
            final HttpService httpService = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
            final WebConsoleServlet registry = new WebConsoleServlet();
            httpService.registerServlet(WebConsoleServlet.CONTEXT, registry, new Hashtable<>(), null);
            return registry;
        }

        @Override
        protected void activated(final WebConsoleServlet servlet) throws InvalidSyntaxException {
            servlet.discoverServices();
        }

        @Override
        protected void cleanupService(final WebConsoleServlet serviceInstance, final boolean stopBundle) throws Exception {

        }
    }

    @SpecialUse
    public WebConsoleActivator(){
        super(new WebConsoleServletProvider());
    }

    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
    }

    private String getResourceBase(){
        final URL resourceRoot = getClass().getClassLoader().getResource("webapp");
        assert resourceRoot != null;
        return resourceRoot.toExternalForm();
    }

    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        // final ConfigurationAdmin configAdmin = getDependency(RequiredServiceAccessor.class, ConfigurationAdmin.class, dependencies);
        @SuppressWarnings("unchecked")
        final HttpService httpService = getDependency(RequiredServiceAccessor.class, HttpService.class, dependencies);
        acceptWithContextClassLoader(getClass().getClassLoader(),
                httpService,
                (publisher) -> publisher.registerServlet(STATIC_SERVLET_CONTEXT, new DefaultServlet(),
                        new Hashtable<>(ImmutableMap
                                .of("resourceBase", getResourceBase(), "pathInfoOnly", "true")
                        ), null));
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, httpService);
    }

    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) throws Exception {
        final HttpService httpService = activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY);
        httpService.unregister(STATIC_SERVLET_CONTEXT);
    }

    @Override
    protected void shutdown() {

    }
}