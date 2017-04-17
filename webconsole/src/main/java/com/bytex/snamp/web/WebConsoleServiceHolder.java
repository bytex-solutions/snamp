package com.bytex.snamp.web;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.management.InstanceNotFoundException;
import java.util.Objects;

/**
 * Represents trivial reference to the web console service without publishing it to Servlet Container.
 */
final class WebConsoleServiceHolder extends ServiceHolder<WebConsoleService> implements WebConsoleServiceReference, SafeCloseable {
    private final String serviceName;
    private final BundleContext context;

    WebConsoleServiceHolder(@Nonnull final BundleContext context, final ServiceReference<WebConsoleService> serviceRef) throws InstanceNotFoundException {
        super(context, serviceRef);
        serviceName = Objects.toString(serviceRef.getProperty(WebConsoleService.NAME));
        this.context = context;
    }

    @Override
    public void close() {
        release(context);
    }

    @Override
    public String getName() {
        return serviceName;
    }

    @Override
    public void activate() {

    }

    @Override
    public void addWebEventListener(final WebConsoleSession listener) {
        getService().ifPresent(service -> service.attachSession(listener));
    }


}
