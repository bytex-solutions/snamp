package com.bytex.snamp.supervision.discovery;

import com.bytex.snamp.WeakEventListenerList;

import javax.annotation.Nonnull;

/**
 * Represents basic infrastructure for building customized discovery services.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractResourceDiscoveryService implements ResourceDiscoveryService, AutoCloseable {
    private final WeakEventListenerList<ResourceDiscoveryEventListener, ResourceDiscoveryEvent> listeners;

    protected AbstractResourceDiscoveryService(){
        listeners = WeakEventListenerList.create(ResourceDiscoveryEventListener::resourceChanged);
    }

    @Override
    public final void addDiscoveryEventListener(@Nonnull final ResourceDiscoveryEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public final void removeDiscoveryEventListener(@Nonnull final ResourceDiscoveryEventListener listener) {
        listeners.remove(listener);
    }

    protected final void fireDiscoveryEvent(@Nonnull final ResourceDiscoveryEvent event){
        listeners.fire(event);
    }

    @Override
    public void close() throws Exception {
        listeners.clear();
    }
}
