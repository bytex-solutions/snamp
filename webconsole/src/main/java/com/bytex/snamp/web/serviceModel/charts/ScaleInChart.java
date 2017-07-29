package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.supervision.elasticity.ScalingMetrics;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Represents rate of downscale.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("scaleIn")
public final class ScaleInChart extends ScalingRateChart {
    public ScaleInChart() {
        super(ScalingMetrics::scaleIn);
    }

    @Override
    protected NumericAxis createAxisY() {
        final NumericAxis axis = new NumericAxis();
        axis.setUOM("operations");
        axis.setName("downscale");
        return axis;
    }
}
