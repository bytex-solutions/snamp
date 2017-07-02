package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.Convert;
import com.bytex.snamp.connector.metrics.RangedGaugeFPRecorder;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;

import static com.bytex.snamp.jmx.MetricsConverter.RANGED_GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRangedFP;

/**
 * Represents floating-point gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class RangedGaugeFPFunction extends AggregationFunction<CompositeData> {
    private final RangedGaugeFPRecorder gaugeFP;

    RangedGaugeFPFunction(final double from, final double to) {
        super(RANGED_GAUGE_FP_TYPE);
        gaugeFP = new RangedGaugeFPRecorder("gaugeFunction", from, to);
    }

    /**
     * Detects valid input type for this function.
     *
     * @param index     Parameter position.
     * @param inputType Input type to check.
     * @return {@literal true}, if this function can accept a value of the specified type; otherwise, {@literal false}.
     */
    @Override
    public boolean canAccept(final int index, final OpenType<?> inputType) {
        return index == 0 && NumericFunction.isNumber(inputType);
    }

    @Override
    public CompositeData eval(final EvaluationContext context, final Object... args) {
        if(args.length > 0)
            gaugeFP.accept(Convert.toDouble(args[0]).orElseThrow(NumberFormatException::new));
        return fromRangedFP(gaugeFP);
    }
}
