package com.bytex.snamp.moa.services;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.GraphOfComponents;
import com.bytex.snamp.moa.watching.*;
import com.bytex.snamp.moa.watching.AttributeWatcher;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.bytex.snamp.MapUtils.getValueAsLong;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents analysis service.
 */
final class AnalyticalGateway extends AbstractGateway implements NotificationListener, AnalyticalCenter {
    private static final String HISTORY_SIZE_PARAM = "topologyAnalyzerHistorySize";
    private static final long DEFAULT_HISTORY_SIZE = 10_000L;

    private TopologyAnalysisModule graph;
    private final WatcherModule watchDog;

    AnalyticalGateway(final BundleContext context) {
        super(DistributedServices.getLocalMemberName(context));
        watchDog = new WatcherModule();
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private Logger getLogger() {
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    //<editor-fold desc="Topology analysis">

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final GraphOfComponents graph = this.graph;
        if (graph == null)
            return;
        else if (notification instanceof SpanNotification)
            graph.accept((SpanNotification) notification);
        else if (notification instanceof NotificationContainer)
            handleNotification(((NotificationContainer) notification).get(), handback);
    }

    private void addNotificationListener(final NotificationSupport support){
        support.addNotificationListener(this, null, null);
    }

    @Override
    protected void addResource(final ManagedResourceConnectorClient resourceConnector) {
        final TopologyAnalysisModule graph = this.graph;
        if (graph == null)
            return;
        graph.add(resourceConnector.getComponentName());
        Aggregator.queryAndAccept(resourceConnector, NotificationSupport.class, this::addNotificationListener);
        watchDog.addResource(resourceConnector);
    }

    @Override
    public <E extends Throwable> void visitVertices(final Acceptor<? super ComponentVertex, E> visitor) throws E {
        final TopologyAnalysisModule graph = this.graph;
        if (graph != null)
            graph.forEach(visitor);
    }

    private void removeNotificationListener(final NotificationSupport support) {
        try {
            support.removeNotificationListener(this);
        } catch (final ListenerNotFoundException e) {
            getLogger().log(Level.WARNING, "Unable to remove notification listener", e);
        }
    }

    @Override
    protected void removeResource(final ManagedResourceConnectorClient resourceConnector) {
        final TopologyAnalysisModule graph = this.graph;
        if (graph == null)
            return;
        graph.remove(resourceConnector.getComponentName());
        Aggregator.queryAndAccept(resourceConnector, NotificationSupport.class, this::removeNotificationListener);
        watchDog.removeResource(resourceConnector);
    }

    //</editor-fold>

    //<editor-fold desc="Watchers">
    @Override
    public void addWatcher(final AttributeWatcher listener) {

    }

    @Override
    public void removeWatcher(final AttributeWatcher listener) {

    }

    @Override
    public Collection<AttributeWatcherSettings> getSettings() {
        return null;
    }

    //</editor-fold>

    @Override
    public void reset() {
        final TopologyAnalysisModule graph = this.graph;
        if (graph != null)
            graph.reset();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) watchDog.addAttribute(resourceName, (MBeanAttributeInfo) feature);
        else
            return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        return watchDog.clear(resourceName).stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) watchDog.removeAttribute(resourceName, (MBeanAttributeInfo) feature);
        else
            return null;
    }

    @Override
    protected void start(final Map<String, String> parameters) {
        graph = new TopologyAnalysisModule(getValueAsLong(parameters, HISTORY_SIZE_PARAM, Long::parseLong).orElse(DEFAULT_HISTORY_SIZE));
    }

    @Override
    protected void stop() {
        reset();
        graph = null;
        watchDog.clear();
    }
}
