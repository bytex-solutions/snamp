package com.bytex.snamp.connector.metrics;

/**
 * Represents default implementation of {@link AttributeMetric} interface.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class AttributeMetricRecorder extends AbstractMetric implements AttributeMetric {
    public static final String DEFAULT_NAME = "attributes";
    private static final long serialVersionUID = 8697867855627851983L;
    private final RateRecorder readRate;
    private final RateRecorder writeRate;

    public AttributeMetricRecorder(final String name){
        super(name);
        readRate = new RateRecorder(name);
        writeRate = new RateRecorder(name);
    }

    protected AttributeMetricRecorder(final AttributeMetricRecorder source){
        super(source);
        readRate = source.readRate.clone();
        writeRate = source.writeRate.clone();
    }

    public AttributeMetricRecorder(){
        this(DEFAULT_NAME);
    }

    @Override
    public AttributeMetricRecorder clone() {
        return new AttributeMetricRecorder(this);
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
    public final Rate writes() {
        return writeRate;
    }

    /**
     * Gets rate of attribute reads.
     *
     * @return Rate of attribute reads.
     */
    @Override
    public final Rate reads() {
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
