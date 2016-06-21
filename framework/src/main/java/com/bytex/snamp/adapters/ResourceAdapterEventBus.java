package com.bytex.snamp.adapters;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.Internal;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.EntryReader;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class ResourceAdapterEventBus {
    private static final ExecutorService EVENT_EXECUTOR =
            Executors.newSingleThreadExecutor(new GroupedThreadFactory("ADAPTER_EVENT_BUS"));

    private static final class AdapterEventHandler implements EntryReader<String, ResourceAdapterEventListener, ExceptionPlaceholder> {
        private final String adapterName;
        private final ResourceAdapterEvent event;

        private AdapterEventHandler(final String adapterName,
                                    final ResourceAdapterEvent event) {
            this.adapterName = adapterName;
            this.event = event;
        }

        @Override
        public boolean read(final String adapterName, final ResourceAdapterEventListener listener) {
            if (Objects.equals(this.adapterName, adapterName))
                if (EVENT_EXECUTOR.isTerminated())
                    listener.handle(event);
                else EVENT_EXECUTOR.execute(() -> listener.handle(event));
            return true;
        }
    }

    private static final Multimap<String, WeakReference<ResourceAdapterEventListener>> listeners =
            HashMultimap.create(10, 3);

    private ResourceAdapterEventBus(){
    }

    @Internal
    static boolean disableAsyncMode(final TimeSpan terminationTimeout) throws InterruptedException {
        EVENT_EXECUTOR.shutdown();
        return EVENT_EXECUTOR.awaitTermination(terminationTimeout.duration, terminationTimeout.unit);
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
                                             final ResourceAdapterEvent event) {
        synchronized (listeners) {
            WeakMultimap.iterate(listeners, new AdapterEventHandler(adapterName, event));
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
