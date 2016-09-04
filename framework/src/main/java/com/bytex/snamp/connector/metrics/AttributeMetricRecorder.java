package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;

import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents default implementation of {@link AttributeMetric} interface.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class AttributeMetricRecorder extends AbstractMetric implements AttributeMetric {
    public static final String DEFAULT_NAME = "attributes";
    private final AtomicLong totalReads = new AtomicLong(0L);
    private final AtomicLong totalWrites = new AtomicLong(0L);
    private final EnumMap<MetricsInterval, TimeLimitedLong> statForReads = new EnumMap<>(MetricsInterval.class);
    private final EnumMap<MetricsInterval, TimeLimitedLong> statForWrites = new EnumMap<>(MetricsInterval.class);

    public AttributeMetricRecorder(final String name){
        super(name);
        for(final MetricsInterval interval: MetricsInterval.values()) {
            statForReads.put(interval, interval.createdAdder(0L));
            statForWrites.put(interval, interval.createdAdder(0L));
        }
    }

    public AttributeMetricRecorder(){
        this(DEFAULT_NAME);
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
    public long getTotalNumberOfReads() {
        return totalReads.get();
    }

    /**
     * Gets number of reads for all attributes for the last time.
     *
     * @param interval Interval of time.
     * @return A number of reads for all attributes.
     */
    @Override
    public long getLastNumberOfReads(final MetricsInterval interval) {
        return statForReads.get(interval).getAsLong();
    }

    /**
     * Gets total number of writes for all attributes.
     *
     * @return A number of writes for all attributes.
     */
    @Override
    public long getTotalNumberOfWrites() {
        return totalWrites.get();
    }

    /**
     * Gets total number of writes for all attributes for the last time.
     *
     * @param interval Interval of time.
     * @return A number of writes for all attributes.
     */
    @Override
    public long getLastNumberOfWrites(final MetricsInterval interval) {
        return statForWrites.get(interval).getAsLong();
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
