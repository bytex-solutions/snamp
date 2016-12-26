package com.bytex.snamp.web;

import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Represents trivial reference to the web console service without publishing it to Servlet Container.
 */
final class WebConsoleServiceHolder extends ServiceHolder<WebConsoleService> implements WebConsoleServiceReference {

    WebConsoleServiceHolder(final BundleContext context, final ServiceReference<WebConsoleService> serviceRef){
        super(context, serviceRef);
    }

    @Override
    public void close() {
        release(getClass().getClassLoader());
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void activate() {

    }
}
