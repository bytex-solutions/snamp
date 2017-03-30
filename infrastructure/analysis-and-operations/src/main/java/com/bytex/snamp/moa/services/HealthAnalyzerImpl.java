package com.bytex.snamp.moa.services;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.configuration.internal.CMSupervisorParser;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.checkers.InvalidAttributeCheckerException;
import com.bytex.snamp.connector.supervision.HealthStatusChangedEvent;
import com.bytex.snamp.connector.supervision.HealthStatusEventListener;
import com.bytex.snamp.connector.supervision.HealthStatus;
import com.bytex.snamp.connector.supervision.triggers.InvalidTriggerException;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.modeling.AttributeValue;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.osgi.service.cm.ConfigurationException;

import javax.annotation.Nonnull;
import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HealthAnalyzerImpl extends ModelOfAttributes<AttributeWatcher> implements HealthAnalyzer, HealthStatusEventListener {
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
        private final UpdatableGroupWatcher watcher;

        private WatcherUpdaterTask(final HealthAnalyzerImpl module, final String componentName, final UpdatableGroupWatcher watcher){
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

    private final Multimap<String, String> groupToResourceMap;
    private final Map<String, UpdatableGroupWatcher> watchers;
    private StatusUpdater statusUpdater;
    private final ExecutorService threadPool;
    private final CMSupervisorParser watcherParser;
    private final WeakEventListenerList<HealthStatusEventListener, HealthStatusChangedEvent> statusListeners;

    HealthAnalyzerImpl(final ExecutorService threadPool, final CMSupervisorParser watcherParser) {
        super(ResourceGroup.class, ResourceGroup.ATTRIBUTES);
        groupToResourceMap = HashMultimap.create();
        this.threadPool = Objects.requireNonNull(threadPool);
        this.watcherParser = Objects.requireNonNull(watcherParser);
        watchers = new HashMap<>();
        statusListeners = WeakEventListenerList.create(HealthStatusEventListener::statusChanged);
    }

    @Nonnull
    @Override
    public ImmutableMap<String, UpdatableGroupWatcher> getConfiguration() {
        return readLock.apply(ResourceGroup.WATCHERS, watchers, ImmutableMap::copyOf);
    }

    @Override
    public String getPersistentID(){
        return "";
    }

    private void updateWatcher(final String groupName, final UpdatableGroupWatcher watcher) throws TimeoutException, InterruptedException {
        final ImmutableSet<String> resources = readLock.apply(ResourceGroup.RESOURCE_MAP, groupToResourceMap, groupName, (m, n) -> ImmutableSet.copyOf(m.get(n)), null);
        final Collection<AttributeValue> attributes = new LinkedList<>();
        for (final String resourceName : resources) {
            final boolean threadAlive = forEachAttribute((attributeName, attributeWatcher) -> {
                try {
                    attributeWatcher.readAttribute(attributes);
                } catch (final AttributeNotFoundException ignored) {
                    //do not add attribute to the attribute list
                } catch (final JMException e) {
                    watcher.updateStatus(resourceName, e);
                }
                return !Thread.interrupted();
            });
            if (threadAlive)
                watcher.updateStatus(resourceName, attributes);
            attributes.clear(); //help GC
        }
    }

    private void updateWatchers() {
        for (final Map.Entry<String, UpdatableGroupWatcher> entry : watchers.entrySet()) {
            final WatcherUpdaterTask task = new WatcherUpdaterTask(this, entry.getKey(), entry.getValue());
            threadPool.submit(task);
        }
    }

    @Override
    protected AttributeWatcher createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
        return new AttributeWatcher(metadata);
    }

    private static void removeAllWatchers(final Map<String, UpdatableGroupWatcher> watchers){
        watchers.values().forEach(UpdatableGroupWatcher::close);
        watchers.clear();
    }

    @Override
    protected void cleared() {
        writeLock.accept(ResourceGroup.RESOURCE_MAP, groupToResourceMap, Multimap::clear);
        writeLock.accept(ResourceGroup.WATCHERS, watchers, HealthAnalyzerImpl::removeAllWatchers);
        statusListeners.clear();
    }

    private static void addResource(final Multimap<String, String> groupToResourceMap, final ManagedResourceConnectorClient resource){
        groupToResourceMap.put(resource.getGroupName(), resource.getManagedResourceName());
    }

    void addResource(final ManagedResourceConnectorClient resource) {
        writeLock.accept(ResourceGroup.RESOURCE_MAP,
                groupToResourceMap,
                resource,
                HealthAnalyzerImpl::addResource);
    }

    void removeResource(final ManagedResourceConnectorClient resource) {
        final String resourceName = resource.getManagedResourceName();
        try (final SafeCloseable ignored = writeLock.acquireLock(ResourceGroup.RESOURCE_MAP)) {
            groupToResourceMap.remove(resource.getGroupName(), resourceName);
        }
        readLock.accept(ResourceGroup.WATCHERS, watchers, watchers -> watchers.values().forEach(watcher -> watcher.removeResource(resourceName)));
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        readLock.accept(ResourceGroup.WATCHERS, watchers, watchers -> watchers.values().forEach(UpdatableGroupWatcher::reset));
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
            final UpdatableGroupWatcher watcher = watchers.get(groupName);
            return watcher == null ? null : watcher.getStatus();
        });
    }

    /**
     * Adds listener of health status.
     *
     * @param listener Listener of health status to add.
     */
    @Override
    public void addHealthStatusEventListener(final HealthStatusEventListener listener) {
        statusListeners.add(listener);
    }

    /**
     * Removes listener of health status.
     *
     * @param listener Listener of health status to remove.
     */
    @Override
    public void removeHealthStatusEventListener(final HealthStatusEventListener listener) {
        statusListeners.remove(listener);
    }

    @Override
    public void statusChanged(final HealthStatusChangedEvent event) {
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

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    private void addWatcher(final String groupName, final SupervisorConfiguration watcherConfig) {
        final UpdatableGroupWatcher watcher;
        try {
            watcher = new UpdatableGroupWatcher(watcherConfig, this);
        } catch (final InvalidTriggerException | InvalidAttributeCheckerException e) {
            getLogger().log(Level.SEVERE, "Unable to instantiate group watcher for group " + groupName, e);
            return;
        }
        watchers.put(groupName, watcher);
    }

    private void addWatchers(final Map<String, ? extends SupervisorConfiguration> configuration) {
        removeAllWatchers(watchers);
        configuration.forEach(this::addWatcher);
    }

    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null)   //remove all watchers
            writeLock.accept(ResourceGroup.WATCHERS, watchers, HealthAnalyzerImpl::removeAllWatchers);
        else {
            final Map<String, ? extends SupervisorConfiguration> watchersConfiguration;
            try {
                watchersConfiguration = watcherParser.parse(properties);
            } catch (final IOException e) {
                throw new ConfigurationException("ALL", "Invalid structure of the dictionary", e);
            }
            writeLock.accept(ResourceGroup.WATCHERS, watchersConfiguration, this::addWatchers);
        }
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
