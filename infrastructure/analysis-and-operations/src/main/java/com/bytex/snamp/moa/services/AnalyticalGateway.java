package com.bytex.snamp.moa.services;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.configuration.internal.CMManagedResourceGroupWatcherParser;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.moa.topology.GraphOfComponents;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
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
    private static final String WATCH_PERIOD_PARAM = "watchPeriod";
    private static final long DEFAULT_HISTORY_SIZE = 5_000L;
    private static final long DEFAULT_WATCH_PERIOD = 1000L;

    private TopologyAnalysisImpl graph;
    private final HealthAnalyzerImpl watchDog;

    AnalyticalGateway(final BundleContext context, final ExecutorService threadPool, final CMManagedResourceGroupWatcherParser watcherParser) {
        super(DistributedServices.getLocalMemberName(context));
        watchDog = new HealthAnalyzerImpl(threadPool, watcherParser);
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
    protected void resourceAdded(final ManagedResourceConnectorClient resourceConnector) {
        Optional.ofNullable(graph).ifPresent(graph -> graph.add(resourceConnector.getGroupName()));
        Aggregator.queryAndAccept(resourceConnector, NotificationSupport.class, this::addNotificationListener);
        watchDog.addResource(resourceConnector);
    }

    private void removeNotificationListener(final NotificationSupport support) {
        try {
            support.removeNotificationListener(this);
        } catch (final ListenerNotFoundException e) {
            getLogger().log(Level.WARNING, "Unable to remove notification listener", e);
        }
    }

    @Override
    protected void resourceRemoved(final ManagedResourceConnectorClient resourceConnector) {
        Optional.ofNullable(graph).ifPresent(graph -> graph.remove(resourceConnector.getGroupName()));
        Aggregator.queryAndAccept(resourceConnector, NotificationSupport.class, this::removeNotificationListener);
        watchDog.removeResource(resourceConnector);
    }

    //</editor-fold>

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
        long param = getValueAsLong(parameters, HISTORY_SIZE_PARAM, Long::parseLong).orElse(DEFAULT_HISTORY_SIZE);
        graph = new TopologyAnalysisImpl(param);
        param = getValueAsLong(parameters, WATCH_PERIOD_PARAM, Long::parseLong).orElse(DEFAULT_WATCH_PERIOD);
        watchDog.startWatching(Duration.ofMillis(param));
    }

    @Override
    protected void stop() throws InterruptedException {
        Optional.ofNullable(graph).ifPresent(TopologyAnalyzer::reset);
        graph = null;
        watchDog.stopWatching();
    }

    @Override
    @Aggregation
    public TopologyAnalysisImpl getTopologyAnalyzer() {
        return graph;
    }

    @Override
    @Aggregation
    public HealthAnalyzer getHealthAnalyzer() {
        return watchDog;
    }
}
