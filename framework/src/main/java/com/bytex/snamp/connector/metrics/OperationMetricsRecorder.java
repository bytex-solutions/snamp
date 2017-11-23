package com.bytex.snamp.connector.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents default implementation of interface {@link OperationMetrics}.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@ThreadSafe
public class OperationMetricsRecorder extends AbstractMetric implements OperationMetrics {
    public static final String DEFAULT_NAME = "operations";
    private static final long serialVersionUID = 2944470493827345462L;
    private final RateRecorder invocationsRate;

    public OperationMetricsRecorder(final String name){
        super(name);
        invocationsRate = new RateRecorder(name);
    }

    protected OperationMetricsRecorder(final OperationMetricsRecorder source){
        super(source);
        invocationsRate = source.invocationsRate.clone();
    }

    @Override
    public OperationMetricsRecorder clone() {
        return new OperationMetricsRecorder(this);
    }

    public OperationMetricsRecorder(){
        this(DEFAULT_NAME);
    }

    public void update(){
        invocationsRate.mark();
    }

    /**
     * Gets rate of all invocations.
     *
     * @return Rate of all invocations.
     */
    @Override
    public final Rate invocations() {
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
