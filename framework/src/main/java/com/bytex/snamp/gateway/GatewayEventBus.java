package com.bytex.snamp.gateway;

import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.framework.BundleContext;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static com.bytex.snamp.internal.Utils.getBundleContext;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GatewayEventBus {
    private static final LazySoftReference<ExecutorService> LOCAL_EVENT_EXECUTOR = new LazySoftReference<>();

    private static final Multimap<String, WeakReference<GatewayEventListener>> listeners =
            HashMultimap.create(10, 3);

    private static ExecutorService getEventExecutor() {
        final BundleContext context = getBundleContext(GatewayEventBus.class);
        if (context != null) {
            final ExecutorService result = ThreadPoolRepository.getDefaultThreadPool(context);
            if (result != null)
                return result;
        }
        final Supplier<? extends ExecutorService> DEFAULT_EXECUTOR_FACTORY = () -> newSingleThreadExecutor(new GroupedThreadFactory("GATEWAY_EVENT_BUS"));
        return LOCAL_EVENT_EXECUTOR.lazyGet(DEFAULT_EXECUTOR_FACTORY);
    }

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

    private static void fireEventListeners(final String gatewayType,
                                           final GatewayEvent event) {
        synchronized (listeners) {
            WeakMultimap.iterate(listeners, (type, listener) -> {
                if (Objects.equals(gatewayType, type))
                    getEventExecutor().execute(() -> listener.handle(event));
                return true;
            });
        }
    }

    static void notifyInstanceStopped(final String gatewayType, final Gateway gatewayInstance) {
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is stopped", gatewayType, gatewayInstance.getInstanceName()));
        fireEventListeners(gatewayType, new GatewayStoppedEvent(gatewayInstance));
    }

    static void notifyInstanceStarted(final String gatewayType, final Gateway gatewayInstance){
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is started", gatewayType, gatewayInstance.getInstanceName()));
        fireEventListeners(gatewayType, new GatewayStartedEvent(gatewayInstance));
    }

    static void notifyInstanceUpdating(final String gatewayType, final Gateway gatewayInstance){
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is updating", gatewayType, gatewayInstance.getInstanceName()));
        fireEventListeners(gatewayType, new GatewayUpdatingEvent(gatewayInstance));
    }

    static void notifyInstanceUpdated(final String gatewayType, final Gateway gatewayInstance) {
        gatewayInstance.getLogger().info(String.format("Gateway %s/%s is updated", gatewayType, gatewayInstance.getInstanceName()));
        fireEventListeners(gatewayType, new GatewayUpdatedEvent(gatewayInstance));
    }
}
