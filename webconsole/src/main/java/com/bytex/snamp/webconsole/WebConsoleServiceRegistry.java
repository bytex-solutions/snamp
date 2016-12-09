package com.bytex.snamp.webconsole;

import com.bytex.snamp.internal.Utils;
import org.osgi.framework.*;
import com.bytex.snamp.webconsole.serviceModel.WebConsoleService;

import java.io.IOException;
import java.util.HashMap;

/**
 * Represents registry of all registered {@link WebConsoleService}s.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleServiceRegistry extends HashMap<String, WebConsoleServiceHolder> implements ServiceListener, AutoCloseable, Constants {
    private static final long serialVersionUID = -4185687019766073277L;

    WebConsoleServiceRegistry() throws InvalidSyntaxException {
        getBundleContext().addServiceListener(this, String.format("(%s=%s)", OBJECTCLASS, WebConsoleService.class.getName()));
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    private synchronized void serviceChanged(final int type, final ServiceReference<WebConsoleService> serviceRef) {
        final String name = WebConsoleServiceHolder.getName(serviceRef);
        switch (type) {
            case ServiceEvent.UNREGISTERING:
                final WebConsoleServiceHolder holder = remove(name);
                if (holder != null)
                    try {
                        holder.get().close();
                    } catch (final IOException e) {

                    } finally {
                        holder.release(getBundleContext());
                    }
        }
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
        values().forEach(holder -> holder.release(getBundleContext()));
        clear();
    }
}
