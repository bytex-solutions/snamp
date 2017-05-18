package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.metrics.Rate;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ScalingMetrics extends Metric {
    /**
     * Gets downscale rate.
     * @return Downscale rate.
     */
    Rate scaleIn();

    /**
     * Gets upscale rate.
     * @return Upscale rate.
     */
    Rate scaleOut();
    
    @Override
    ScalingMetrics clone();
}
