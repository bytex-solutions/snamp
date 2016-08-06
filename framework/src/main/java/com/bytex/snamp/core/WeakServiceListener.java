package com.bytex.snamp.core;

import com.bytex.snamp.WeakEventListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class WeakServiceListener extends WeakEventListener<ServiceListener, ServiceEvent> implements ServiceListener {
    WeakServiceListener(final ServiceListener listener){
        super(listener);
    }

    @Override
    protected void invoke(final ServiceListener listener, final ServiceEvent event) {
        listener.serviceChanged(event);
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param event The {@code ServiceEvent} object.
     */
    @Override
    public void serviceChanged(final ServiceEvent event) {
        invoke(event);
    }
}
