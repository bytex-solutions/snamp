package com.bytex.snamp.connector.metrics;

/**
 * Represents default implementation of {@link AttributeMetric} interface.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class AttributeMetricRecorder extends AbstractMetric implements AttributeMetric {
    public static final String DEFAULT_NAME = "attributes";
    private final RateRecorder readRate;
    private final RateRecorder writeRate;

    public AttributeMetricRecorder(final String name){
        super(name);
        readRate = new RateRecorder(name);
        writeRate = new RateRecorder(name);
    }

    public AttributeMetricRecorder(){
        this(DEFAULT_NAME);
    }

    /**
     * Marks single read.
     */
    public void updateReads(){
        readRate.mark();
    }

    /**
     * Marks single write.
     */
    public void updateWrites(){
        writeRate.mark();
    }

    /**
     * Gets rate of attribute writes.
     *
     * @return Rate of attribute writes.
     */
    @Override
    public Rate writes() {
        return writeRate;
    }

    /**
     * Gets rate of attribute reads.
     *
     * @return Rate of attribute reads.
     */
    @Override
    public Rate reads() {
        return readRate;
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        writeRate.reset();
        readRate.reset();
    }
}
