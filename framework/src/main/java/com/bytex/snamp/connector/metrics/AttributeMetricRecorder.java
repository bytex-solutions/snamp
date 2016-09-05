package com.bytex.snamp.connector.metrics;

import com.bytex.snamp.concurrent.TimeLimitedLong;

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
    private final MetricsIntervalMap<TimeLimitedLong> statForReads;
    private final MetricsIntervalMap<TimeLimitedLong> statForWrites;

    public AttributeMetricRecorder(final String name){
        super(name);
        statForReads = new MetricsIntervalMap<>(interval -> interval.createdAdder(0L));
        statForWrites = new MetricsIntervalMap<>(interval -> interval.createdAdder(0L));
    }

    public AttributeMetricRecorder(){
        this(DEFAULT_NAME);
    }

    /**
     * Marks single read.
     */
    public void updateReads(){
        totalReads.incrementAndGet();
        statForReads.values().forEach(TimeLimitedLong::updateByOne);
    }

    /**
     * Marks single write.
     */
    public void updateWrites(){
        totalWrites.incrementAndGet();
        statForWrites.values().forEach(TimeLimitedLong::updateByOne);
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
        statForWrites.applyToAllIntervals(TimeLimitedLong::reset);
        statForReads.applyToAllIntervals(TimeLimitedLong::reset);
    }
}
