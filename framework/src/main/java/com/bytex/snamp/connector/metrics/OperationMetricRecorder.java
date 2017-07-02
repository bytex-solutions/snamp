package com.bytex.snamp.connector.metrics;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Represents default implementation of interface {@link OperationMetric}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public class OperationMetricRecorder extends AbstractMetric implements OperationMetric {
    public static final String DEFAULT_NAME = "operations";
    private static final long serialVersionUID = 2944470493827345462L;
    private final RateRecorder invocationsRate;

    public OperationMetricRecorder(final String name){
        super(name);
        invocationsRate = new RateRecorder(name);
    }

    protected OperationMetricRecorder(final OperationMetricRecorder source){
        super(source);
        invocationsRate = source.invocationsRate.clone();
    }

    @Override
    public OperationMetricRecorder clone() {
        return new OperationMetricRecorder(this);
    }

    public OperationMetricRecorder(){
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
