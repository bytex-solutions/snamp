package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.Convert;
import com.bytex.snamp.connector.metrics.RangedGauge64Recorder;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;

import static com.bytex.snamp.jmx.MetricsConverter.RANGED_GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromRanged64;

/**
 * Represents floating-point gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class RangedGaugeIntFunction extends AggregationFunction<CompositeData> {
    private final RangedGauge64Recorder gaugeFP;

    RangedGaugeIntFunction(final long from, final long to) {
        super(RANGED_GAUGE_FP_TYPE);
        gaugeFP = new RangedGauge64Recorder("gaugeFunction", from, to);
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
    public CompositeData invoke(final NameResolver resolver, final Object... args) {
        if(args.length > 0)
            gaugeFP.accept(Convert.toLong(args[0]));
        return fromRanged64(gaugeFP);
    }
}
