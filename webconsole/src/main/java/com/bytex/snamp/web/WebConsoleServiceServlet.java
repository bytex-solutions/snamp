package com.bytex.snamp.web;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

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
    private static final String ROOT_CONTEXT = "/snamp/console/api";
    private static final long serialVersionUID = -5668618198214458448L;
    private final ServiceHolder<WebConsoleService> serviceHolder;
    private final String servletContext;
    private final String serviceName;
    private final HttpService whiteboard;

    private WebConsoleServiceServlet(final ServiceHolder<WebConsoleService> serviceReference,
                                     final HttpService publisher,
                                     final WebSecurityFilter securityFilter) {
        super(createConfig(serviceReference.getService(), securityFilter));
        this.serviceHolder = serviceReference;
        servletContext = ROOT_CONTEXT + serviceReference.getProperty(WebConsoleService.URL_CONTEXT);
        serviceName = Objects.toString(serviceReference.getProperty(WebConsoleService.NAME));
        this.whiteboard = Objects.requireNonNull(publisher);
    }

    WebConsoleServiceServlet(final BundleContext context, final ServiceReference<WebConsoleService> serviceReference,
                             final HttpService publisher,
                             final WebSecurityFilter securityFilter) {
        this(new ServiceHolder<>(context, serviceReference), publisher, securityFilter);
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    @Override
    public void activate() throws Exception {
        whiteboard.registerServlet(servletContext, this, new Hashtable<>(), null);
    }

    @Override
    public WebConsoleService get(){
        return serviceHolder.getService();
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
    public void close() {
        whiteboard.unregister(servletContext);
        serviceHolder.release(getBundleContext());
        destroy();
    }
}
