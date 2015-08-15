package com.bytex.snamp.adapters;

import com.bytex.snamp.concurrent.AsyncEventListener;
import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.internal.WeakMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ResourceAdapterEventBus {
    private static final Multimap<String, WeakReference<ResourceAdapterEventListener>> listeners =
            HashMultimap.create(10, 3);
    private static final ExecutorService eventExecutor =
            Executors.newSingleThreadExecutor(new GroupedThreadFactory("ADAPTER_EVENTS"));


    private ResourceAdapterEventBus(){

    }

    static boolean addEventListener(final String adapterName,
                                    final ResourceAdapterEventListener listener){
        if(adapterName == null || adapterName.isEmpty() || listener == null) return false;
        synchronized (listeners){
            return WeakMultimap.put(listeners, adapterName, listener);
        }
    }

    static boolean removeEventListener(final String adapterName,
                                       final ResourceAdapterEventListener listener){
        if(adapterName == null || listener == null) return false;
        synchronized (listeners){
            return WeakMultimap.remove(listeners, adapterName, listener) > 0;
        }
    }

    private static void fireAdapterListeners(final String adapterName,
                                             final ResourceAdapterEvent event){
        synchronized (listeners){
            WeakMultimap.gc(listeners);
            for(final WeakReference<ResourceAdapterEventListener> listenerRef: listeners.get(adapterName)){
                final ResourceAdapterEventListener listener = listenerRef.get();
                if(listener instanceof AsyncEventListener)
                    eventExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            listener.handle(event);
                        }
                    });
                else if(listener != null) listener.handle(event);
            }
        }
    }

    static void notifyAdapterStopped(final String adapterName, final ResourceAdapter adapter) {
        adapter.getLogger().info(String.format("Adapter %s is stopped", adapter.getInstanceName()));
        fireAdapterListeners(adapterName, new ResourceAdapterStoppedEvent(adapter));
    }

    static void notifyAdapterStarted(final String adapterName, final ResourceAdapter adapter){
        adapter.getLogger().info(String.format("Adapter %s is started", adapter.getInstanceName()));
        fireAdapterListeners(adapterName, new ResourceAdapterStartedEvent(adapter));
    }

    static void notifyAdapterUpdating(final String adapterName, final ResourceAdapter adapter){
        adapter.getLogger().info(String.format("Adapter %s is updating", adapter.getInstanceName()));
        fireAdapterListeners(adapterName, new ResourceAdapterUpdatingEvent(adapter));
    }

    static void notifyAdapterUpdated(final String adapterName, final ResourceAdapter adapter) {
        adapter.getLogger().info(String.format("Adapter %s is updated", adapter.getInstanceName()));
        fireAdapterListeners(adapterName, new ResourceAdapterUpdatedEvent(adapter));
    }
}
