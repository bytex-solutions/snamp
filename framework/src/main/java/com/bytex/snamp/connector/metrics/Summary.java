package com.bytex.snamp.connector.metrics;

import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

/**
 * Represents summary metrics.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Summary {
    private Summary(){
        throw new InstantiationError();
    }

    /**
     * Aggregates {@link Rate} from a collection of other metrics.
     * @param name The name of new metrics.
     * @param rates A supplier of rates to aggregate.
     * @return A new aggregated {@link Rate}.
     */
    public static Rate summaryRate(final String name, final Supplier<? extends Stream<? extends Rate>> rates){
        return new Rate() {
            private long sum(final ToLongFunction<? super Rate> extractor){
                return rates.get().mapToLong(extractor).sum();
            }

            private double avg(final ToDoubleFunction<? super Rate> extractor){
                return rates.get().mapToDouble(extractor).average().orElse(0D);
            }

            @Override
            public long getTotalRate() {
                return sum(Rate::getTotalRate);
            }

            @Override
            public long getLastRate(final MetricsInterval interval) {
                return sum(rate -> rate.getLastRate(interval));
            }

            @Override
            public double getLastMeanRate(final MetricsInterval interval) {
                return avg(rate -> rate.getLastMeanRate(interval));
            }

            @Override
            public double getMeanRate(final MetricsInterval interval) {
                return avg(rate -> rate.getMeanRate(interval));
            }

            @Override
            public long getMaxRate(final MetricsInterval interval) {
                return sum(rate -> rate.getMaxRate(interval));
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void reset() {
                rates.get().forEach(Rate::reset);
            }
        };
    }
}
