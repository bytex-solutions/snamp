package com.bytex.snamp.web;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;

import javax.ws.rs.core.Application;
import java.util.Hashtable;
import java.util.Objects;

/**
 * Created by Роман on 26.12.2016.
 */
public class WebConsoleServiceServlet extends ServletContainer implements WebConsoleServiceReference {
    static final String ROOT_CONTEXT = "/snamp/console/api";
    private static final long serialVersionUID = -5668618198214458448L;
    private final ServiceHolder<WebConsoleService> serviceHolder;
    private final String servletContext;
    private final String serviceName;

    private WebConsoleServiceServlet(final ServiceHolder<WebConsoleService> serviceReference, final WebSecurityFilter securityFilter) {
        super(createConfig(serviceReference.getService(), securityFilter));
        this.serviceHolder = serviceReference;
        servletContext = ROOT_CONTEXT + serviceReference.getProperty(WebConsoleService.URL_CONTEXT);
        serviceName = getName(serviceReference);
    }

    WebConsoleServiceServlet(final BundleContext context, final ServiceReference<WebConsoleService> serviceReference, final WebSecurityFilter securityFilter) {
        this(new ServiceHolder<>(context, serviceReference), securityFilter);
    }

    static boolean isResourceModel(final ServiceReference<WebConsoleService> serviceRef) {
        return serviceRef.getProperty(WebConsoleService.URL_CONTEXT) instanceof String;
    }

    static String getName(final ServiceReference<WebConsoleService> serviceRef){
        return Objects.toString(serviceRef.getProperty(WebConsoleService.NAME));
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    private <E extends Throwable> void withHttpService(final Acceptor<? super HttpService, E> handler) throws E {
        final ServiceHolder<HttpService> httpService = ServiceHolder.tryCreate(getBundleContext(), HttpService.class);
        if(httpService == null)
            throw new IllegalStateException("Dependency on service HttpService could not be resolved");
        try{
            handler.accept(httpService.get());
        } finally {
            httpService.release(getBundleContext());
        }
    }

    @Override
    public void activate() throws Exception {
        withHttpService(whiteboard -> whiteboard.registerServlet(servletContext, this, new Hashtable<>(), null));
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
        withHttpService(whiteboard -> whiteboard.unregister(servletContext));
        serviceHolder.release(getBundleContext());
        destroy();
    }
}
