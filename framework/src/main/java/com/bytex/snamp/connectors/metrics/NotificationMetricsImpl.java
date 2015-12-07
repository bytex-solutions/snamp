package com.bytex.snamp.connectors.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationMetricsImpl implements NotificationMetrics {
    private final AtomicLong totalEmitted = new AtomicLong(0L);
    private final EnumMap<MetricsInterval, LongAccumulator> statOfEmitted = new EnumMap<>(MetricsInterval.class);

    NotificationMetricsImpl(){
        for(final MetricsInterval interval: MetricsInterval.values())
            statOfEmitted.put(interval, interval.createAccumulator());
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
        return statOfEmitted.get(interval).longValue();
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
