package com.bytex.snamp.connector.metrics;

/**
 * Represents default implementation of interface {@link OperationMetric}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class OperationMetricRecorder extends AbstractMetric implements OperationMetric {
    public static final String DEFAULT_NAME = "operations";
    private final RateRecorder invocationsRate;

    public OperationMetricRecorder(final String name){
        super(name);
        invocationsRate = new RateRecorder(name);
    }

    public OperationMetricRecorder(){
        this(DEFAULT_NAME);
    }

    public void update(){
        invocationsRate.update();
    }

    /**
     * Gets rate of all invocations.
     *
     * @return Rate of all invocations.
     */
    @Override
    public Rate invocations() {
        return invocationsRate;
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        invocationsRate.reset();
    }
}
