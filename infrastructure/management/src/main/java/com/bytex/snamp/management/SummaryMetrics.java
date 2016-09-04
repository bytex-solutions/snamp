package com.bytex.snamp.management;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.metrics.*;
import com.bytex.snamp.core.ServiceHolder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.ToLongFunction;

/**
 * Provides metrics across all active resource connector.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public final class SummaryMetrics extends ImmutableMetrics {

    private static abstract class AbstractSummaryMetric<M extends Metric> implements Metric {
        private final Class<M> metricsType;
        private final BundleContext context;

        private AbstractSummaryMetric(final Class<M> mt, final BundleContext context){
            this.metricsType = mt;
            this.context = Objects.requireNonNull(context);
        }

        private static <M extends Metric> long getMetric(final MetricsSupport reader,
                                                         final Class<M> metricType,
                                                         final ToLongFunction<? super M> fn) {
            final Iterator<? extends M> iterator = reader.getMetrics(metricType).iterator();
            return iterator.hasNext() ? fn.applyAsLong(iterator.next()) : 0L;
        }

        final long aggregateMetrics(final ToLongFunction<? super M> reader) {
            long result = 0L;
            for (final ServiceReference<ManagedResourceConnector> connectorRef : ManagedResourceConnectorClient.getConnectors(context).values()) {
                final ServiceHolder<ManagedResourceConnector> connector = new ServiceHolder<>(context, connectorRef);
                try {
                    final MetricsSupport metrics = connector.get().queryObject(MetricsSupport.class);
                    if (metrics != null) result += getMetric(metrics, metricsType, reader);
                } finally {
                    connector.release(context);
                }
            }
            return result;
        }

        @Override
        public final void reset() {
            aggregateMetrics(metrics -> {
                metrics.reset();
                return 0L;
            });
        }
    }

    private static final class SummaryAttributeMetric extends AbstractSummaryMetric<AttributeMetric> implements AttributeMetric {
        private static final String NAME = "summaryAttributes";

        private SummaryAttributeMetric(final BundleContext context) {
            super(AttributeMetric.class, context);
        }

        @Override
        public long getTotalNumberOfReads() {
            return aggregateMetrics(AttributeMetric::getTotalNumberOfReads);
        }

        @Override
        public long getLastNumberOfReads(final MetricsInterval interval) {
            return aggregateMetrics(metrics -> metrics.getLastNumberOfReads(interval));
        }

        @Override
        public long getTotalNumberOfWrites() {
            return aggregateMetrics(AttributeMetric::getTotalNumberOfWrites);
        }

        @Override
        public long getLastNumberOfWrites(final MetricsInterval interval) {
            return aggregateMetrics(metrics -> metrics.getLastNumberOfWrites(interval));
        }

        /**
         * Gets name of this metric.
         *
         * @return Name of this metric.
         */
        @Override
        public String getName() {
            return NAME;
        }
    }

    private static final class SummaryNotificationMetric extends AbstractSummaryMetric<NotificationMetric> implements NotificationMetric {
        private static final String NAME = "summaryNotifications";

        private SummaryNotificationMetric(final BundleContext context) {
            super(NotificationMetric.class, context);
        }

        @Override
        public long getTotalNumberOfNotifications() {
            return aggregateMetrics(NotificationMetric::getTotalNumberOfNotifications);
        }

        @Override
        public long getLastNumberOfEmitted(final MetricsInterval interval) {
            return aggregateMetrics(metrics -> metrics.getLastNumberOfEmitted(interval));
        }

        /**
         * Gets name of this metric.
         *
         * @return Name of this metric.
         */
        @Override
        public String getName() {
            return NAME;
        }
    }

    private static final class SummaryOperationMetric extends AbstractSummaryMetric<OperationMetric> implements OperationMetric {
        private static final String NAME = "summaryOperations";

        private SummaryOperationMetric(final BundleContext context) {
            super(OperationMetric.class, context);
        }

        @Override
        public long getTotalNumberOfInvocations() {
            return aggregateMetrics(OperationMetric::getTotalNumberOfInvocations);
        }

        @Override
        public long getLastNumberOfInvocations(final MetricsInterval interval) {
            return aggregateMetrics(metrics -> metrics.getLastNumberOfInvocations(interval));
        }

        /**
         * Gets name of this metric.
         *
         * @return Name of this metric.
         */
        @Override
        public String getName() {
            return NAME;
        }
    }

    public SummaryMetrics(final BundleContext context) {
        super(new SummaryAttributeMetric(context), new SummaryNotificationMetric(context), new SummaryOperationMetric(context));
    }
}
