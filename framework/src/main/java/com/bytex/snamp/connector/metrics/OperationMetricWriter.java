package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents default implementation of interface {@link OperationMetric}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class OperationMetricWriter extends AbstractMetric implements OperationMetric {
    public static final String DEFAULT_NAME = "operations";
    private final AtomicLong totalInvocations = new AtomicLong(0L);
    private final EnumMap<MetricsInterval, LongAccumulator> statOfInvocations = new EnumMap<>(MetricsInterval.class);

    public OperationMetricWriter(final String name){
        super(name);
        for(final MetricsInterval interval: MetricsInterval.values())
            statOfInvocations.put(interval, interval.createdAdder());
    }

    public OperationMetricWriter(){
        this(DEFAULT_NAME);
    }

    public void update(){
        totalInvocations.incrementAndGet();
        statOfInvocations.values().forEach(accumulator -> accumulator.update(1L));
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
