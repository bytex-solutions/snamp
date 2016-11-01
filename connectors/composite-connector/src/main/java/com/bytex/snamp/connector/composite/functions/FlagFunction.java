package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.connector.metrics.FlagRecorder;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenType;

import static com.bytex.snamp.jmx.MetricsConverter.FLAG_TYPE;
import static com.bytex.snamp.jmx.MetricsConverter.fromFlag;

/**
 * Represents flag recorder.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class FlagFunction extends AggregationFunction<CompositeData> {
    private final FlagRecorder flag;

    FlagFunction() {
        super(FLAG_TYPE);
        flag = new FlagRecorder("flagRecorder");
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
        if(args.length > 0){
            final Object arg = args[0];
            if(arg instanceof Boolean)
                flag.accept((Boolean)arg);
            else if(arg instanceof Number)
                flag.accept(((Number)arg).longValue() != 0);
        }
        return fromFlag(flag);
    }
}
