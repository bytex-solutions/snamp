package com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Switch;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.core.ServiceHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import java.util.Objects;

/**
 * Provides metrics across all active resource connectors.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class SummaryMetrics extends AbstractAggregator implements MetricsReader {
    private interface MetricFunction<M extends Metrics>{
        long getMetric(final M metrics);
    }

    private static abstract class AbstractMetrics<M extends Metrics> implements Metrics{
        private final Class<M> metricsType;
        private final BundleContext context;

        private AbstractMetrics(final Class<M> mt, final BundleContext context){
            this.metricsType = mt;
            this.context = Objects.requireNonNull(context);
        }

        private static <M extends Metrics> long getMetric(final MetricsReader reader,
                                                          final Class<M> metricType,
                                                          final MetricFunction<? super M> fn){
            final M metrics = reader.queryObject(metricType);
            return metrics != null ? fn.getMetric(metrics) : 0L;
        }

        protected final long aggregateMetrics(final MetricFunction<? super M> reader) {
            long result = 0L;
            for (final ServiceReference<ManagedResourceConnector> connectorRef : ManagedResourceConnectorClient.getConnectors(context).values()) {
                final ServiceHolder<ManagedResourceConnector> connector = new ServiceHolder<>(context, connectorRef);
                try {
                    final MetricsReader metrics = connector.get().queryObject(MetricsReader.class);
                    if (metrics != null) result += getMetric(metrics, metricsType, reader);
                } finally {
                    connector.release(context);
                }
            }
            return result;
        }

        @Override
        public final void reset() {
            aggregateMetrics(new MetricFunction<Metrics>() {
                @Override
                public long getMetric(final Metrics metrics) {
                    metrics.reset();
                    return 0L;
                }
            });
        }
    }

    private static final class SummaryAttributeMetrics extends AbstractMetrics<AttributeMetrics> implements AttributeMetrics {

        private SummaryAttributeMetrics(final BundleContext context) {
            super(AttributeMetrics.class, context);
        }

        @Override
        public long getNumberOfReads() {
            return aggregateMetrics(new MetricFunction<AttributeMetrics>() {
                @Override
                public long getMetric(final AttributeMetrics metrics) {
                    return metrics.getNumberOfReads();
                }
            });
        }

        @Override
        public long getNumberOfReads(final MetricsInterval interval) {
            return aggregateMetrics(new MetricFunction<AttributeMetrics>() {
                @Override
                public long getMetric(final AttributeMetrics metrics) {
                    return metrics.getNumberOfReads(interval);
                }
            });
        }

        @Override
        public long getNumberOfWrites() {
            return aggregateMetrics(new MetricFunction<AttributeMetrics>() {
                @Override
                public long getMetric(final AttributeMetrics metrics) {
                    return metrics.getNumberOfWrites();
                }
            });
        }

        @Override
        public long getNumberOfWrites(final MetricsInterval interval) {
            return aggregateMetrics(new MetricFunction<AttributeMetrics>() {
                @Override
                public long getMetric(final AttributeMetrics metrics) {
                    return metrics.getNumberOfWrites(interval);
                }
            });
        }
    }

    private static final class SummaryNotificationMetrics extends AbstractMetrics<NotificationMetrics> implements NotificationMetrics {
        private SummaryNotificationMetrics(final BundleContext context) {
            super(NotificationMetrics.class, context);
        }

        @Override
        public long getNumberOfEmitted() {
            return aggregateMetrics(new MetricFunction<NotificationMetrics>() {
                @Override
                public long getMetric(final NotificationMetrics metrics) {
                    return metrics.getNumberOfEmitted();
                }
            });
        }

        @Override
        public long getNumberOfEmitted(final MetricsInterval interval) {
            return aggregateMetrics(new MetricFunction<NotificationMetrics>() {
                @Override
                public long getMetric(final NotificationMetrics metrics) {
                    return metrics.getNumberOfEmitted(interval);
                }
            });
        }
    }

    private static final class SummaryOperationMetrics extends AbstractMetrics<OperationMetrics> implements OperationMetrics {
        private SummaryOperationMetrics(final BundleContext context) {
            super(OperationMetrics.class, context);
        }

        @Override
        public long getNumberOfInvocations() {
            return aggregateMetrics(new MetricFunction<OperationMetrics>() {
                @Override
                public long getMetric(final OperationMetrics metrics) {
                    return metrics.getNumberOfInvocations();
                }
            });
        }

        @Override
        public long getNumberOfInvocations(final MetricsInterval interval) {
            return aggregateMetrics(new MetricFunction<OperationMetrics>() {
                @Override
                public long getMetric(final OperationMetrics metrics) {
                    return metrics.getNumberOfInvocations(interval);
                }
            });
        }
    }

    @Aggregation
    private final SummaryAttributeMetrics attributes;
    @Aggregation
    private final SummaryNotificationMetrics notifications;
    @Aggregation
    private final SummaryOperationMetrics operations;

    public SummaryMetrics(final BundleContext context){
        attributes = new SummaryAttributeMetrics(context);
        notifications = new SummaryNotificationMetrics(context);
        operations = new SummaryOperationMetrics(context);
    }

    @Override
    public Metrics getMetrics(final Class<? extends MBeanFeatureInfo> featureType) {
        return new Switch<Class<? extends MBeanFeatureInfo>, Metrics>()
                .equals(MBeanAttributeInfo.class, attributes)
                .equals(MBeanNotificationInfo.class, notifications)
                .equals(MBeanOperationInfo.class, operations)
                .apply(featureType);
    }

    @Override
    public void resetAll() {
        attributes.reset();
        notifications.reset();
        operations.reset();
    }
}
