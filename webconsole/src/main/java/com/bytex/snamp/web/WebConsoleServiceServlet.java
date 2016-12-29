package com.bytex.snamp.web;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.WebEventListener;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

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
    private static final String ROOT_CONTEXT = "/snamp/web/api";
    private static final long serialVersionUID = -5668618198214458448L;
    private transient final ServiceHolder<WebConsoleService> serviceHolder;
    private final String servletContext;
    private final String serviceName;
    private transient ServiceRegistration<Servlet> registration;

    private WebConsoleServiceServlet(final ServiceHolder<WebConsoleService> serviceReference,
                                     final WebSecurityFilter securityFilter) {
        super(createConfig(serviceReference.getService(), securityFilter));
        this.serviceHolder = serviceReference;
        servletContext = ROOT_CONTEXT + serviceReference.getProperty(WebConsoleService.URL_CONTEXT);
        serviceName = Objects.toString(serviceReference.getProperty(WebConsoleService.NAME));
    }

    WebConsoleServiceServlet(final BundleContext context, final ServiceReference<WebConsoleService> serviceReference,
                             final WebSecurityFilter securityFilter) {
        this(new ServiceHolder<>(context, serviceReference), securityFilter);
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    @Override
    public void activate() {
        final Hashtable<String, String> identity = new Hashtable<>();
        identity.put("alias", servletContext);
        registration = getBundleContext().registerService(Servlet.class, this, identity);
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
    public void addWebEventListener(final WebEventListener listener) {
        final WebConsoleService service = serviceHolder.getService();
        if (service != null)
            service.addWebEventListener(listener);
    }

    @Override
    public void close() {
        if (registration != null) {
            registration.unregister();
            registration = null;
        }
        serviceHolder.release(getBundleContext());
        destroy();
    }
}
