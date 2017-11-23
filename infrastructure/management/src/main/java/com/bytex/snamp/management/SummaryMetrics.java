package com.bytex.snamp.management;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.metrics.*;
import org.osgi.framework.BundleContext;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides metrics across all active resource connector.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
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
            return ManagedResourceConnectorClient.selector().getResources(context).stream()
                    .map(resourceName -> {
                        final Optional<ManagedResourceConnectorClient> connector = ManagedResourceConnectorClient.tryCreate(context, resourceName);
                        if (connector.isPresent())
                            try (final ManagedResourceConnectorClient client = connector.get()) {
                                return client.queryObject(MetricsSupport.class);
                            }
                        else
                            return Optional.<MetricsSupport>empty();
                    })
                    .filter(Optional::<MetricsSupport>isPresent)
                    .map(Optional::<MetricsSupport>get)
                    .flatMap(support -> StreamSupport.stream(support.getMetrics(metricsType).spliterator(), false).map(reader));
        }

        @Override
        public final void reset() {
            toStream(Function.identity()).forEach(Metric::reset);
        }
    }

    private static final class SummaryAttributeMetrics extends SummaryMetric<AttributeMetrics> implements AttributeMetrics {
        private static final String NAME = "summaryAttributes";
        private final Rate readRate;
        private final Rate writeRate;

        private SummaryAttributeMetrics(final BundleContext context) {
            super(AttributeMetrics.class, context);
            readRate = Summary.summaryRate(NAME, this::readsStream);
            writeRate = Summary.summaryRate(NAME, this::writesStream);
        }

        private SummaryAttributeMetrics(final SummaryAttributeMetrics source){
            super(source);
            readRate = source.readRate.clone();
            writeRate = source.writeRate.clone();
        }

        @Override
        public SummaryAttributeMetrics clone() {
            return new SummaryAttributeMetrics(this);
        }

        private Stream<Rate> readsStream() {
            return toStream(AttributeMetrics::reads);
        }

        private Stream<Rate> writesStream(){
            return toStream(AttributeMetrics::writes);
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

    private static final class SummaryNotificationMetrics extends SummaryMetric<NotificationMetrics> implements NotificationMetrics, Supplier<Stream<Rate>> {
        private static final String NAME = "summaryNotifications";
        private final Rate rate;

        private SummaryNotificationMetrics(final BundleContext context) {
            super(NotificationMetrics.class, context);
            rate = Summary.summaryRate(NAME, this);
        }

        private SummaryNotificationMetrics(final SummaryNotificationMetrics source){
            super(source);
            rate = source.rate.clone();
        }

        @Override
        public SummaryNotificationMetrics clone() {
            return new SummaryNotificationMetrics(this);
        }

        @Override
        public Stream<Rate> get() {
            return toStream(NotificationMetrics::notifications);
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

    private static final class SummaryOperationMetrics extends SummaryMetric<OperationMetrics> implements OperationMetrics, Supplier<Stream<Rate>> {
        private static final String NAME = "summaryOperations";
        private final Rate rate;

        private SummaryOperationMetrics(final BundleContext context) {
            super(OperationMetrics.class, context);
            rate = Summary.summaryRate(NAME, this);
        }

        private SummaryOperationMetrics(final SummaryOperationMetrics source){
            super(source);
            rate = source.rate.clone();
        }

        @Override
        public SummaryOperationMetrics clone() {
            return new SummaryOperationMetrics(this);
        }

        @Override
        public Rate invocations() {
            return rate;
        }

        @Override
        public Stream<Rate> get() {
            return toStream(OperationMetrics::invocations);
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    public SummaryMetrics(final BundleContext context) {
        super(new SummaryAttributeMetrics(context), new SummaryNotificationMetrics(context), new SummaryOperationMetrics(context));
    }
}
