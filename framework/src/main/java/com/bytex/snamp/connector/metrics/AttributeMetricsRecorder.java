package com.bytex.snamp.connector.metrics;

/**
 * Represents default implementation of interface {@link AttributeMetrics}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class AttributeMetricsRecorder extends AbstractMetric implements AttributeMetrics {
    public static final String DEFAULT_NAME = "attributes";
    private static final long serialVersionUID = 8697867855627851983L;
    private final RateRecorder readRate;
    private final RateRecorder writeRate;

    public AttributeMetricsRecorder(final String name){
        super(name);
        readRate = new RateRecorder(name);
        writeRate = new RateRecorder(name);
    }

    protected AttributeMetricsRecorder(final AttributeMetricsRecorder source){
        super(source);
        readRate = source.readRate.clone();
        writeRate = source.writeRate.clone();
    }

    public AttributeMetricsRecorder(){
        this(DEFAULT_NAME);
    }

    @Override
    public AttributeMetricsRecorder clone() {
        return new AttributeMetricsRecorder(this);
    }

    /**
     * Marks single read.
     */
    public final void updateReads(){
        readRate.mark();
    }

    /**
     * Marks single write.
     */
    public final void updateWrites(){
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
