package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents default implementation of interface {@link NotificationMetric}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class NotificationMetricWriter extends AbstractMetric implements NotificationMetric {
    public static final String DEFAULT_NAME = "notifications";
    private final AtomicLong totalEmitted = new AtomicLong(0L);
    private final EnumMap<MetricsInterval, LongAccumulator> statOfEmitted = new EnumMap<>(MetricsInterval.class);

    public NotificationMetricWriter(final String name){
        super(name);
        for(final MetricsInterval interval: MetricsInterval.values())
            statOfEmitted.put(interval, interval.createdAdder(0L));
    }

    public NotificationMetricWriter(){
        this(DEFAULT_NAME);
    }

    public void update(){
        totalEmitted.incrementAndGet();
        statOfEmitted.values().forEach(accumulator -> accumulator.update(1L));
    }

    /**
     * Gets total number of all emitted notifications.
     *
     * @return A number of all emitted notifications.
     */
    @Override
    public long getNumberOfEmitted() {
        return totalEmitted.get();
    }

    @Override
    public long getNumberOfEmitted(final MetricsInterval interval) {
        return statOfEmitted.get(interval).getAsLong();
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        totalEmitted.set(0L);
        for(final MetricsInterval interval: MetricsInterval.values())
            statOfEmitted.get(interval).reset();
    }
}
