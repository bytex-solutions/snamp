package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.LongAccumulator;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents default implementation of {@link AttributeMetrics} interface.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class AttributeMetricsWriter implements AttributeMetrics {
    private final AtomicLong totalReads = new AtomicLong(0L);
    private final AtomicLong totalWrites = new AtomicLong(0L);
    private final EnumMap<MetricsInterval, LongAccumulator> statForReads = new EnumMap<>(MetricsInterval.class);
    private final EnumMap<MetricsInterval, LongAccumulator> statForWrites = new EnumMap<>(MetricsInterval.class);

    public AttributeMetricsWriter(){
        for(final MetricsInterval interval: MetricsInterval.values()) {
            statForReads.put(interval, interval.createAccumulator());
            statForWrites.put(interval, interval.createAccumulator());
        }
    }

    /**
     * Marks single read.
     */
    public void updateReads(){
        totalReads.incrementAndGet();
        statForReads.values().forEach(accumulator -> accumulator.update(1L));
    }

    /**
     * Marks single write.
     */
    public void updateWrites(){
        totalWrites.incrementAndGet();
        statForWrites.values().forEach(accumulator -> accumulator.update(1L));
    }

    /**
     * Gets total number of reads for all attributes.
     *
     * @return A number of reads for all attributes.
     */
    @Override
    public long getNumberOfReads() {
        return totalReads.get();
    }

    /**
     * Gets number of reads for all attributes for the last time.
     *
     * @param interval Interval of time.
     * @return A number of reads for all attributes.
     */
    @Override
    public long getNumberOfReads(final MetricsInterval interval) {
        return statForReads.get(interval).longValue();
    }

    /**
     * Gets total number of writes for all attributes.
     *
     * @return A number of writes for all attributes.
     */
    @Override
    public long getNumberOfWrites() {
        return totalWrites.get();
    }

    /**
     * Gets total number of writes for all attributes for the last time.
     *
     * @param interval Interval of time.
     * @return A number of writes for all attributes.
     */
    @Override
    public long getNumberOfWrites(final MetricsInterval interval) {
        return statForWrites.get(interval).longValue();
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        totalReads.set(0L);
        totalWrites.set(0L);
        for(final MetricsInterval interval: MetricsInterval.values()){
            statForWrites.get(interval).reset();
            statForReads.get(interval).reset();
        }
    }
}
