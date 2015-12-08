package com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents default implementation of interface {@link OperationMetrics}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class OperationMetricsWriter implements OperationMetrics {
    private final AtomicLong totalInvocations = new AtomicLong(0L);
    private final EnumMap<MetricsInterval, LongAccumulator> statOfInvocations = new EnumMap<>(MetricsInterval.class);

    public OperationMetricsWriter(){
        for(final MetricsInterval interval: MetricsInterval.values())
            statOfInvocations.put(interval, interval.createAccumulator());
    }

    public void update(){
        totalInvocations.incrementAndGet();
        for(final LongAccumulator accumulator: statOfInvocations.values())
            accumulator.update(1L);
    }

    @Override
    public long getNumberOfInvocations() {
        return totalInvocations.get();
    }

    @Override
    public long getNumberOfInvocations(final MetricsInterval interval) {
        return statOfInvocations.get(interval).longValue();
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        totalInvocations.set(0L);
        for(final MetricsInterval interval: MetricsInterval.values())
            statOfInvocations.get(interval).reset();
    }
}
