package com.bytex.snamp.gateway;

import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.LazyValue;
import com.bytex.snamp.LazyValueFactory;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.framework.BundleContext;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static com.bytex.snamp.internal.Utils.getBundleContext;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GatewayEventBus {
    private static final LazyValue<ExecutorService> LOCAL_EVENT_EXECUTOR = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(() -> newSingleThreadExecutor(new GroupedThreadFactory("GATEWAY_EVENT_BUS")));

    private static final Multimap<String, WeakReference<GatewayEventListener>> listeners =
            HashMultimap.create(10, 3);

    private static ExecutorService getEventExecutor() {
        final BundleContext context = getBundleContext(GatewayEventBus.class);
        ExecutorService result = context == null ? LOCAL_EVENT_EXECUTOR.get() : ThreadPoolRepository.getDefaultThreadPool(context);
        if (result == null)
            result = LOCAL_EVENT_EXECUTOR.get();
        return result;
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
