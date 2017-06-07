package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.connector.metrics.Metric;
import com.bytex.snamp.connector.metrics.Rate;

/**
 * Gets metrics associated with scaling operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ScalingMetrics extends Metric {
    String DEFAULT_NAME = "scaling";
            
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

    /**
     * Creates clone of this object.
     * @return Deep clone of this object.
     */
    @Override
    ScalingMetrics clone();
}
