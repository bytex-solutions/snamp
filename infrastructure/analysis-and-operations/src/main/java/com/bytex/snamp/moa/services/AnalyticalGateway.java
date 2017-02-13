package com.bytex.snamp.moa.services;

import static com.bytex.snamp.MapUtils.getValueAsLong;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.connector.ClusteredResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.GraphOfComponents;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Represents analysis service.
 */
final class AnalyticalGateway extends AbstractGateway implements NotificationListener, AnalyticalCenter {
    private static final String HISTORY_SIZE_PARAM = "topologyAnalyzerHistorySize";
    private static final long DEFAULT_HISTORY_SIZE = 10_000L;

    private TopologyAnalysisModule graph;

    AnalyticalGateway(final BundleContext context) {
        super(DistributedServices.getLocalMemberName(context));
    }

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

    @Override
    protected void resourceAdded(final ManagedResourceConnector resourceConnector) {
        final TopologyAnalysisModule graph = this.graph;
        if (graph == null)
            return;
        final ClusteredResourceConnector clusteredResourceConnector = resourceConnector.queryObject(ClusteredResourceConnector.class);
        if (clusteredResourceConnector != null)
            graph.addResource(clusteredResourceConnector);
        final NotificationSupport notificationSupport = resourceConnector.queryObject(NotificationSupport.class);
        if (notificationSupport != null)
            notificationSupport.addNotificationListener(this, null, null);
    }

    @Override
    public void reset() {
        final TopologyAnalysisModule graph = this.graph;
        if (graph != null)
            graph.reset();
    }

    @Override
    public <E extends Throwable> void visitVertices(final Acceptor<? super ComponentVertex, E> visitor) throws E {
        final TopologyAnalysisModule graph = this.graph;
        if (graph != null)
            graph.forEach(visitor);
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private Logger getLogger() {
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    @Override
    protected void resourceRemoved(final ManagedResourceConnector resourceConnector) {
        final TopologyAnalysisModule graph = this.graph;
        if (graph == null)
            return;
        final ClusteredResourceConnector clusteredResourceConnector = resourceConnector.queryObject(ClusteredResourceConnector.class);
        if (clusteredResourceConnector != null)
            graph.removeResource(clusteredResourceConnector);
        final NotificationSupport notificationSupport = resourceConnector.queryObject(NotificationSupport.class);
        if (notificationSupport != null)
            try {
                notificationSupport.removeNotificationListener(this);
            } catch (final ListenerNotFoundException e) {
                getLogger().log(Level.WARNING, "Unable to remove notification listener", e);
            }
    }

    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) {
        return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        return null;
    }

    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
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
    }
}
