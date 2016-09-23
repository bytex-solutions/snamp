package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.connector.metrics.GaugeFPRecorder;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

import static com.bytex.snamp.jmx.MetricsConverter.GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromGaugeFP;

/**
 * Represents floating-point gauge.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GaugeFunction extends AggregationFunction<CompositeData> {
    private final GaugeFPRecorder gaugeFP;

    GaugeFunction() {
        super(GAUGE_FP_TYPE);
        gaugeFP = new GaugeFPRecorder("gaugeFunction");
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
    public CompositeData invoke(final NameResolver resolver, final Object... args) throws OpenDataException {
        if(args.length > 0 && args[0] instanceof Number){
            final Number num = (Number) args[0];
            gaugeFP.accept(num.doubleValue());
        }
        return fromGaugeFP(gaugeFP);
    }
}
