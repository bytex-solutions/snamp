package com.bytex.snamp.webconsole;

import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.webconsole.serviceModel.WebConsoleService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleServiceHolder extends ServiceHolder<WebConsoleService> {
    WebConsoleServiceHolder(final BundleContext context, final ServiceReference<WebConsoleService> serviceRef) throws IllegalArgumentException {
        super(context, serviceRef);
    }

    String getName() {
        return getName(this);
    }

    static String getName(final ServiceReference<WebConsoleService> serviceRef){
        return (String) serviceRef.getProperty(WebConsoleService.NAME);
    }
}
