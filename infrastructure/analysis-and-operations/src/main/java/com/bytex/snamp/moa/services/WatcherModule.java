package com.bytex.snamp.moa.services;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.bytex.snamp.moa.watching.ComponentWatcher;
import com.bytex.snamp.moa.watching.ComponentWatchersRepository;
import com.bytex.snamp.moa.watching.WatcherService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.management.MBeanAttributeInfo;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WatcherModule extends ModelOfAttributes<AttributeWatcher> implements WatcherService {
    private static final class ConcurrentWatchersRepository extends ConcurrentHashMap<String, UpdatableComponentWatcher> implements ComponentWatchersRepository<UpdatableComponentWatcher> {
        private static final long serialVersionUID = -8757794702136258515L;

        @Override
        public boolean addAndConsume(final String key, final Consumer<? super UpdatableComponentWatcher> handler) {
            final UpdatableComponentWatcher newWatcher = new UpdatableComponentWatcher();
            final UpdatableComponentWatcher oldWatcher = putIfAbsent(key, newWatcher);
            if (oldWatcher == null) {
                handler.accept(newWatcher);
                return true;
            } else {
                handler.accept(oldWatcher);
                return false;
            }
        }

        @Override
        public UpdatableComponentWatcher getOrAdd(final String componentName) {
            UpdatableComponentWatcher watcher = new UpdatableComponentWatcher();
            watcher = firstNonNull(putIfAbsent(componentName, watcher), watcher);
            return watcher;
        }
    }

    private static final class StatusUpdater extends WeakRepeater<WatcherModule> {

        private StatusUpdater(final Duration period, final WatcherModule module) {
            super(period, module);
        }

        @Override
        protected void doAction(final WatcherModule input) throws Exception {
            input.updateWatchers();
        }

        @Override
        protected String generateThreadName() {
            return "UpdateStatusThread";
        }
    }

    private final Multimap<String, String> componentToResourceMap;
    private StatusUpdater statusUpdater;
    private final ConcurrentWatchersRepository watchers;

    WatcherModule(final ExecutorService threadPool) {
        componentToResourceMap = HashMultimap.create();
        watchers = new ConcurrentWatchersRepository();

    }

    void updateWatchers(){

    }

    @Override
    protected AttributeWatcher createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
        return new AttributeWatcher(metadata);
    }

    @Override
    protected void cleared() {
        writeLock.accept(DEFAULT_RESOURCE_GROUP, componentToResourceMap, Multimap::clear);
        watchers.values().forEach(UpdatableComponentWatcher::clear);
        watchers.clear();
    }

    private static void addResource(final Multimap<String, String> componentToResourceMap, final ManagedResourceConnectorClient resource){
        componentToResourceMap.put(resource.getComponentName(), resource.getManagedResourceName());
    }

    void addResource(final ManagedResourceConnectorClient resource) {
        writeLock.accept(DEFAULT_RESOURCE_GROUP,
                componentToResourceMap,
                resource,
                WatcherModule::addResource);
    }

    void removeResource(final ManagedResourceConnectorClient resource) {
        final String resourceName = resource.getManagedResourceName();
        try (final SafeCloseable ignored = writeLock.acquireLock(DEFAULT_RESOURCE_GROUP)) {
            componentToResourceMap.remove(resource.getComponentName(), resourceName);
        }
        watchers.values().forEach(watcher -> watcher.removeResource(resourceName));
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        watchers.values().forEach(ComponentWatcher::reset);
    }

    @Override
    public ComponentWatchersRepository<?> getComponentsWatchers() {
        return watchers;
    }

    void startWatching(final Duration updatePeriod){
        statusUpdater = new StatusUpdater(updatePeriod, this);
        statusUpdater.run();
    }

    void stopWatching() throws InterruptedException {
        try {
            statusUpdater.close();
        } finally {
            statusUpdater = null;
        }
    }
}
