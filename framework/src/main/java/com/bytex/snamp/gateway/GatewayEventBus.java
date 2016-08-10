package com.bytex.snamp.gateway;

import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GatewayEventBus {
    private static final ExecutorService EVENT_EXECUTOR =
            Executors.newSingleThreadExecutor(new GroupedThreadFactory("ADAPTER_EVENT_BUS"));

    private static final Multimap<String, WeakReference<GatewayEventListener>> listeners =
            HashMultimap.create(10, 3);

    private GatewayEventBus(){
        throw new InstantiationError();
    }

    static boolean addEventListener(final String gatewayType,
                                    final GatewayEventListener listener){
        if(isNullOrEmpty(gatewayType) || listener == null) return false;
        synchronized (listeners){
            return WeakMultimap.put(listeners, gatewayType, listener);
        }
    }

    static boolean removeEventListener(final String gatewayType,
                                       final GatewayEventListener listener){
        if(isNullOrEmpty(gatewayType) || listener == null) return false;
        synchronized (listeners){
            return WeakMultimap.remove(listeners, gatewayType, listener) > 0;
        }
    }

    private static void fireAdapterListeners(final String gatewayType,
                                             final GatewayEvent event) {
        synchronized (listeners) {
            WeakMultimap.iterate(listeners, (type, listener) -> {
                if (Objects.equals(gatewayType, type))
                    if (EVENT_EXECUTOR.isTerminated())
                        listener.handle(event);
                    else EVENT_EXECUTOR.execute(() -> listener.handle(event));
                return true;
            });
        }
    }

    static void notifyAdapterStopped(final String gatewayType, final Gateway gatewayInstance) {
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is stopped", gatewayType, gatewayInstance.getInstanceName()));
        fireAdapterListeners(gatewayType, new GatewayStoppedEvent(gatewayInstance));
    }

    static void notifyAdapterStarted(final String gatewayType, final Gateway gatewayInstance){
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is started", gatewayType, gatewayInstance.getInstanceName()));
        fireAdapterListeners(gatewayType, new GatewayStartedEvent(gatewayInstance));
    }

    static void notifyAdapterUpdating(final String gatewayType, final Gateway gatewayInstance){
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is updating", gatewayType, gatewayInstance.getInstanceName()));
        fireAdapterListeners(gatewayType, new GatewayUpdatingEvent(gatewayInstance));
    }

    static void notifyAdapterUpdated(final String gatewayType, final Gateway gatewayInstance) {
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is updated", gatewayType, gatewayInstance.getInstanceName()));
        fireAdapterListeners(gatewayType, new GatewayUpdatedEvent(gatewayInstance));
    }
}
