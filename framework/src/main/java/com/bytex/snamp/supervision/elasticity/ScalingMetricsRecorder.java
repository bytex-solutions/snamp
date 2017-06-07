package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.connector.metrics.AbstractMetric;
import com.bytex.snamp.connector.metrics.Rate;
import com.bytex.snamp.connector.metrics.RateRecorder;

/**
 * Represents default implementation of interface {@link ScalingMetrics}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ScalingMetricsRecorder extends AbstractMetric implements ScalingMetrics {
    private static final long serialVersionUID = -133927668958872736L;
    private final RateRecorder scaleIn;
    private final RateRecorder scaleOut;

    protected ScalingMetricsRecorder(final ScalingMetricsRecorder source) {
        super(source);
        scaleIn = source.scaleIn.clone();
        scaleOut = source.scaleOut.clone();
    }

    public ScalingMetricsRecorder(final String name) {
        super(name);
        scaleIn = new RateRecorder(name);
        scaleOut = new RateRecorder(name);
    }

    public ScalingMetricsRecorder(){
        this(DEFAULT_NAME);
    }

    public final void downscale(){
        scaleIn.mark();
    }

    public final void upscale(){
        scaleOut.mark();
    }

    /**
     * Gets downscale rate.
     *
     * @return Downscale rate.
     */
    @Override
    public final Rate scaleIn() {
        return scaleIn;
    }

    /**
     * Gets upscale rate.
     *
     * @return Upscale rate.
     */
    @Override
    public final Rate scaleOut() {
        return scaleOut;
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        scaleOut.reset();
        scaleIn.reset();
    }

    @Override
    public ScalingMetricsRecorder clone() {
        return new ScalingMetricsRecorder(this);
    }
}
