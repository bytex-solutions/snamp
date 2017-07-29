package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.supervision.elasticity.ScalingMetrics;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("scaleOut")
public final class ScaleOutChart extends ScalingRateChart {
    public ScaleOutChart() {
        super(ScalingMetrics::scaleOut);
    }

    @Override
    protected NumericAxis createAxisY() {
        final NumericAxis axis = new NumericAxis();
        axis.setUOM("operations");
        axis.setName("upscale");
        return axis;
    }
}
