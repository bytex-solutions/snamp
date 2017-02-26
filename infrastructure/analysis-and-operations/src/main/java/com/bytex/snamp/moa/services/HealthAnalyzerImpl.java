package com.bytex.snamp.moa.services;

import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.configuration.internal.CMManagedResourceGroupWatcherParser;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.supervision.GroupStatusChangedEvent;
import com.bytex.snamp.connector.supervision.GroupStatusEventListener;
import com.bytex.snamp.connector.supervision.HealthStatus;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.osgi.service.cm.ConfigurationException;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HealthAnalyzerImpl extends ModelOfAttributes<AttributeWatcher> implements HealthAnalyzer, GroupStatusEventListener {
    private enum ResourceGroup{
        ATTRIBUTES,
        WATCHERS,
        RESOURCE_MAP
    }

    private static final class StatusUpdater extends WeakRepeater<HealthAnalyzerImpl> {

        private StatusUpdater(final Duration period, final HealthAnalyzerImpl module) {
            super(period, module);
        }

        @Override
        protected void doAction() throws InterruptedException {
            getReferenceOrTerminate().updateWatchers();
        }

        @Override
        protected String generateThreadName() {
            return "UpdateStatusThread";
        }
    }

    private static final class WatcherUpdaterTask extends WeakReference<HealthAnalyzerImpl> implements Callable<Void>{
        private final String componentName;
        private final UpdatableComponentWatcher watcher;

        private WatcherUpdaterTask(final HealthAnalyzerImpl module, final String componentName, final UpdatableComponentWatcher watcher){
            super(module);
            this.componentName = componentName;
            this.watcher = watcher;
        }

        @Override
        public Void call() throws TimeoutException, InterruptedException {
            final HealthAnalyzerImpl module = get();
            if (module != null)
                module.updateWatcher(componentName, watcher);
            return null;
        }
    }

    private final Multimap<String, String> componentToResourceMap;
    private final Map<String, UpdatableComponentWatcher> watchers;
    private StatusUpdater statusUpdater;
    private final ExecutorService threadPool;
    private final CMManagedResourceGroupWatcherParser watcherParser;
    private final WeakEventListenerList<GroupStatusEventListener, GroupStatusChangedEvent> statusListeners;

    HealthAnalyzerImpl(final ExecutorService threadPool, final CMManagedResourceGroupWatcherParser watcherParser) {
        super(ResourceGroup.class, ResourceGroup.ATTRIBUTES);
        componentToResourceMap = HashMultimap.create();
        this.threadPool = Objects.requireNonNull(threadPool);
        this.watcherParser = Objects.requireNonNull(watcherParser);
        watchers = new HashMap<>();
        statusListeners = WeakEventListenerList.create(GroupStatusEventListener::statusChanged);
    }

    @Override
    public String getPersistentID(){
        return watcherParser.getPersistentID();
    }

    private void updateWatcher(final String componentName, final UpdatableComponentWatcher watcher) throws TimeoutException, InterruptedException {
        final ImmutableSet<String> resources = readLock.apply(ResourceGroup.RESOURCE_MAP, componentToResourceMap, componentName, (m, n) -> ImmutableSet.copyOf(m.get(n)), null);
        final Box<Attribute> attribute = BoxFactory.create(null);   //avoid redundant creation of the box in every iteration inside of the loop
        for (final String resourceName : resources) {
            attribute.reset();
            try {
                processAttribute(resourceName, w -> true, w -> attribute.set(w.getRawValue()));
            } catch (final JMException error) {
                watcher.updateStatus(resourceName, error);
                continue;
            }
            if (attribute.hasValue())
                watcher.updateStatus(resourceName, attribute.get());
        }
    }

    private void updateWatchers() {
        for (final Map.Entry<String, UpdatableComponentWatcher> entry : watchers.entrySet()) {
            final WatcherUpdaterTask task = new WatcherUpdaterTask(this, entry.getKey(), entry.getValue());
            threadPool.submit(task);
        }
    }

    @Override
    protected AttributeWatcher createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
        return new AttributeWatcher(metadata);
    }

    private static void removeAllWatchers(final Map<String, UpdatableComponentWatcher> watchers){
        watchers.values().forEach(UpdatableComponentWatcher::clear);
        watchers.clear();
    }

    @Override
    protected void cleared() {
        writeLock.accept(ResourceGroup.RESOURCE_MAP, componentToResourceMap, Multimap::clear);
        writeLock.accept(ResourceGroup.WATCHERS, watchers, HealthAnalyzerImpl::removeAllWatchers);
        statusListeners.clear();
    }

    private static void addResource(final Multimap<String, String> componentToResourceMap, final ManagedResourceConnectorClient resource){
        componentToResourceMap.put(resource.getGroupName(), resource.getManagedResourceName());
    }

    void addResource(final ManagedResourceConnectorClient resource) {
        writeLock.accept(ResourceGroup.RESOURCE_MAP,
                componentToResourceMap,
                resource,
                HealthAnalyzerImpl::addResource);
    }

    void removeResource(final ManagedResourceConnectorClient resource) {
        final String resourceName = resource.getManagedResourceName();
        try (final SafeCloseable ignored = writeLock.acquireLock(ResourceGroup.RESOURCE_MAP)) {
            componentToResourceMap.remove(resource.getGroupName(), resourceName);
        }
        readLock.accept(ResourceGroup.WATCHERS, watchers, watchers -> watchers.values().forEach(watcher -> watcher.removeResource(resourceName)));
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        readLock.accept(ResourceGroup.WATCHERS, watchers, watchers -> watchers.values().forEach(UpdatableComponentWatcher::reset));
    }

    /**
     * Gets immutable set of groups configured for health check.
     *
     * @return Immutable set of groups configured for health check.
     */
    @Override
    public ImmutableSet<String> getWatchingGroups() {
        return readLock.apply(ResourceGroup.WATCHERS, watchers, watchers -> ImmutableSet.copyOf(watchers.keySet()));
    }

    /**
     * Gets health status of the specified group.
     *
     * @param groupName Group of managed resources.
     * @return Health status of the group; or {@literal null}, if group is not configured for watching.
     */
    @Override
    public HealthStatus getHealthStatus(final String groupName) {
        return readLock.apply(ResourceGroup.WATCHERS, watchers, watchers -> {
            final UpdatableComponentWatcher watcher = watchers.get(groupName);
            return watcher == null ? null : watcher.getStatus();
        });
    }

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     */
    @Override
    public void addHealthStatusEventListener(final GroupStatusEventListener listener) {
        statusListeners.add(listener);
    }

    /**
     * Removes listener of health status.
     *
     * @param listener Listener of health status to remove.
     */
    @Override
    public void removeHealthStatusEventListener(final GroupStatusEventListener listener) {
        statusListeners.remove(listener);
    }

    @Override
    public void statusChanged(final GroupStatusChangedEvent event) {
        statusListeners.fire(event);
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(@Nonnull final Class<T> objectType) {
        return objectType.isInstance(this) ? objectType.cast(this) : null;
    }

    private void addWatcher(final String groupName, final ManagedResourceGroupWatcherConfiguration watcherConfig) {
        watchers.put(groupName, new UpdatableComponentWatcher(watcherConfig, this));
    }

    private void addWatchers(final Map<String, ? extends ManagedResourceGroupWatcherConfiguration> configuration) {
        removeAllWatchers(watchers);
        configuration.forEach(this::addWatcher);
    }

    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {
        final Map<String, ? extends ManagedResourceGroupWatcherConfiguration> watchersConfiguration;
        try{
            watchersConfiguration = watcherParser.parse(properties);
        } catch (final IOException e){
            throw new ConfigurationException("ALL", "Invalid structure of the dictionary", e);
        }
        writeLock.accept(ResourceGroup.WATCHERS, watchersConfiguration, this::addWatchers);
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
