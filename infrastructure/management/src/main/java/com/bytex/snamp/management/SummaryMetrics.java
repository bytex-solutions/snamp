package com.bytex.snamp.management;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.metrics.*;
import org.osgi.framework.BundleContext;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides metrics across all active resource connector.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public final class SummaryMetrics extends ImmutableMetrics {
    private static abstract class SummaryMetric<M extends Metric> implements Metric {
        private final Class<M> metricsType;
        private final BundleContext context;

        private SummaryMetric(final Class<M> mt, final BundleContext context){
            this.metricsType = mt;
            this.context = Objects.requireNonNull(context);
        }

        private SummaryMetric(final SummaryMetric<M> source){
            metricsType = source.metricsType;
            context = source.context;
        }

        @Override
        public abstract SummaryMetric<M> clone();

        final <O> Stream<O> toStream(final Function<? super M, ? extends O> reader) {
            return ManagedResourceConnectorClient.filterBuilder().getResources(context).stream()
                    .flatMap(resourceName -> {
                        final ManagedResourceConnectorClient connector = ManagedResourceConnectorClient.tryCreate(context, resourceName);
                        MetricsSupport metrics;
                        if (connector == null)
                            metrics = null;
                        else
                            try {
                                metrics = connector.queryObject(MetricsSupport.class);
                            } finally {
                                connector.release(context);
                            }
                        return metrics == null ?
                                Stream.empty() :
                                StreamSupport.stream(metrics.getMetrics(metricsType).spliterator(), false).map(reader);
                    });
        }

        @Override
        public final void reset() {
            toStream(Function.identity()).forEach(Metric::reset);
        }
    }

    private static final class SummaryAttributeMetric extends SummaryMetric<AttributeMetric> implements AttributeMetric {
        private static final String NAME = "summaryAttributes";
        private final Rate readRate;
        private final Rate writeRate;

        private SummaryAttributeMetric(final BundleContext context) {
            super(AttributeMetric.class, context);
            readRate = Summary.summaryRate(NAME, this::readsStream);
            writeRate = Summary.summaryRate(NAME, this::writesStream);
        }

        private SummaryAttributeMetric(final SummaryAttributeMetric source){
            super(source);
            readRate = source.readRate.clone();
            writeRate = source.writeRate.clone();
        }

        @Override
        public SummaryAttributeMetric clone() {
            return new SummaryAttributeMetric(this);
        }

        private Stream<Rate> readsStream() {
            return toStream(AttributeMetric::reads);
        }

        private Stream<Rate> writesStream(){
            return toStream(AttributeMetric::writes);
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Rate writes() {
            return writeRate;
        }

        @Override
        public Rate reads() {
            return readRate;
        }
    }

    private static final class SummaryNotificationMetric extends SummaryMetric<NotificationMetric> implements NotificationMetric, Supplier<Stream<Rate>> {
        private static final String NAME = "summaryNotifications";
        private final Rate rate;

        private SummaryNotificationMetric(final BundleContext context) {
            super(NotificationMetric.class, context);
            rate = Summary.summaryRate(NAME, this);
        }

        private SummaryNotificationMetric(final SummaryNotificationMetric source){
            super(source);
            rate = source.rate.clone();
        }

        @Override
        public SummaryNotificationMetric clone() {
            return new SummaryNotificationMetric(this);
        }

        @Override
        public Stream<Rate> get() {
            return toStream(NotificationMetric::notifications);
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Rate notifications() {
            return rate;
        }
    }

    private static final class SummaryOperationMetric extends SummaryMetric<OperationMetric> implements OperationMetric, Supplier<Stream<Rate>> {
        private static final String NAME = "summaryOperations";
        private final Rate rate;

        private SummaryOperationMetric(final BundleContext context) {
            super(OperationMetric.class, context);
            rate = Summary.summaryRate(NAME, this);
        }

        private SummaryOperationMetric(final SummaryOperationMetric source){
            super(source);
            rate = source.rate.clone();
        }

        @Override
        public SummaryOperationMetric clone() {
            return new SummaryOperationMetric(this);
        }

        @Override
        public Rate invocations() {
            return rate;
        }

        @Override
        public Stream<Rate> get() {
            return toStream(OperationMetric::invocations);
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    public SummaryMetrics(final BundleContext context) {
        super(new SummaryAttributeMetric(context), new SummaryNotificationMetric(context), new SummaryOperationMetric(context));
    }
}
