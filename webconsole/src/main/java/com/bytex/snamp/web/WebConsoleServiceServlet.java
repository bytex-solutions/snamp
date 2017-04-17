package com.bytex.snamp.web;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.core.ServiceRegistrationHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.InstanceNotFoundException;
import javax.servlet.Servlet;
import javax.ws.rs.core.Application;
import java.util.Hashtable;
import java.util.Objects;

/**
 * Represents web console service which must be hosted as REST service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
final class WebConsoleServiceServlet extends ServletContainer implements WebConsoleServiceReference {
    private static final class ServletRegistrationHolder extends ServiceRegistrationHolder<Servlet, ServletContainer>{
        private ServletRegistrationHolder(final BundleContext context,
                                          final ServletContainer container,
                                          final String servletContext) {
            super(context, container, identity(servletContext), Servlet.class);
        }

        private static Hashtable<String, ?> identity(final String servletContext){
            final Hashtable<String, String> identity = new Hashtable<>();
            identity.put("alias", servletContext);
            return identity;
        }
    }

    private static final String ROOT_CONTEXT = "/snamp/web/api";
    private static final long serialVersionUID = -5668618198214458448L;
    private transient final WebConsoleServiceHolder serviceHolder;
    private final String servletContext;
    private final String serviceName;
    private ServletRegistrationHolder registration;

    private WebConsoleServiceServlet(final WebConsoleServiceHolder serviceReference,
                                     final WebSecurityFilter securityFilter) {
        super(createConfig(serviceReference.get(), securityFilter));
        this.serviceHolder = serviceReference;
        servletContext = ROOT_CONTEXT + serviceReference.getProperty(WebConsoleService.URL_CONTEXT);
        serviceName = Objects.toString(serviceReference.getProperty(WebConsoleService.NAME));
    }

    WebConsoleServiceServlet(final BundleContext context, final ServiceReference<WebConsoleService> serviceReference,
                             final WebSecurityFilter securityFilter) throws InstanceNotFoundException {
        this(new WebConsoleServiceHolder(context, serviceReference), securityFilter);
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    @Override
    public void activate() {
        registration = new ServletRegistrationHolder(getBundleContext(), this, servletContext);
    }

    @Override
    public WebConsoleService get(){
        return serviceHolder.get();
    }

    @Override
    public String getName(){
        return serviceName;
    }

    private static Application createConfig(final WebConsoleService service, final WebSecurityFilter filter){
        final ResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(service);
        result.getContainerRequestFilters().add(filter);
        result.getContainerResponseFilters().add(filter);
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        return result;
    }

    @Override
    public void addWebEventListener(final WebConsoleSession listener) {
        serviceHolder.addWebEventListener(listener);
    }

    @Override
    public void close() throws Exception {
        try {
            Utils.closeAll(registration, serviceHolder, this::destroy);
        } finally {
            registration = null;
        }
    }
}
