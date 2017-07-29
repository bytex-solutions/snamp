package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.Convert;
import com.bytex.snamp.connector.metrics.Gauge64Recorder;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;

import static com.bytex.snamp.jmx.MetricsConverter.GAUGE_FP_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromGauge64;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class GaugeIntFunction extends AggregationFunction<CompositeData> {
    private final Gauge64Recorder gauge64;

    GaugeIntFunction() {
        super(GAUGE_FP_TYPE);
        gauge64 = new Gauge64Recorder("gaugeFunction");
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
        if (args.length > 0)
            gauge64.accept(Convert.toLong(args[0]).orElseThrow(NumberFormatException::new));
        return fromGauge64(gauge64);
    }
}
