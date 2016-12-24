package com.bytex.snamp.web;

import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.security.web.WebSecurityFilter;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.osgi.framework.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents registry of all registered {@link WebConsoleService}s.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleServlet extends ServletContainer implements ServiceListener, AutoCloseable, Constants {
    static final String CONTEXT = "/snamp/console/api";
    private transient final Map<String, WebConsoleServiceHolder> services;
    private transient final ResourceConfig resourceConfig;

    private WebConsoleServlet(final ResourceConfig resourceConfig) throws InvalidSyntaxException {
        super(resourceConfig);
        this.resourceConfig = Objects.requireNonNull(resourceConfig);

        services = new HashMap<>();
        getBundleContext().addServiceListener(this, String.format("(%s=%s)", OBJECTCLASS, WebConsoleService.class.getName()));
    }

    WebConsoleServlet() throws InvalidSyntaxException {
        this(createResourceConfig());
    }

    private static ResourceConfig createResourceConfig(){
        final ResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new VersionResource());
        final WebSecurityFilter filter = new WebSecurityFilter();
        result.getContainerRequestFilters().add(filter);
        result.getContainerResponseFilters().add(filter);
        result.getFeatures().put("com.sun.jersey.api.json.POJOMappingFeature", true);
        return result;
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    synchronized void discoverServices() throws InvalidSyntaxException {
        final ServiceReference<?>[] refs = getBundleContext().getAllServiceReferences(WebConsoleService.class.getName(), null);
        if (refs != null)
            for (final ServiceReference<?> r : refs)
                serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, r));
    }

    private synchronized void serviceChanged(final int type, final ServiceReference<WebConsoleService> serviceRef) {
        final String name = WebConsoleServiceHolder.getName(serviceRef);
        boolean reloadRequired = false;
        switch (type) {
            case ServiceEvent.MODIFIED_ENDMATCH:
            case ServiceEvent.UNREGISTERING:
                WebConsoleServiceHolder holder = services.remove(name);
                if (holder != null) {
                    resourceConfig.getSingletons().remove(holder.get());
                    try {
                        holder.get().close();
                    } catch (final IOException e) {
                        log(String.format("Unable to change service %s", name), e);
                    } finally {
                        holder.release(getBundleContext());
                        reloadRequired = true;
                    }
                }
                break;
            case ServiceEvent.REGISTERED:
                holder = new WebConsoleServiceHolder(getBundleContext(), serviceRef);
                if (reloadRequired = holder.get().isResourceModel())
                    resourceConfig.getSingletons().add(holder.get());
                break;
            default:
                reloadRequired = false;
                break;
        }
        if (reloadRequired)
            reload();
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (Utils.isInstanceOf(event.getServiceReference(), WebConsoleService.class))
            serviceChanged(event.getType(), (ServiceReference<WebConsoleService>) event.getServiceReference());
    }

    @Override
    public synchronized void close() {
        getBundleContext().removeServiceListener(this);
        services.values().forEach(holder -> holder.release(getBundleContext()));
        services.clear();
    }
}
