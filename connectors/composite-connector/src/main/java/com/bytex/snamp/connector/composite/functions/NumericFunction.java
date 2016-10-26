package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.jmx.WellKnownType;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * Represents abstract class for all functions returning {@code double} result.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class NumericFunction extends AggregationFunction<Double> {
    NumericFunction(){
        super(SimpleType.DOUBLE);
    }

    static boolean isNumber(final OpenType<?> inputType){
        final WellKnownType type = WellKnownType.getType(inputType);
        return type != null && type.isNumber();
    }

    @Override
    public final boolean canAccept(final int index, final OpenType<?> inputType) {
        return index == 0 && isNumber(inputType);
    }

    abstract double getFallbackValue();

    abstract double invoke(final NameResolver resolver, final Number input) throws Exception;

    /**
     * Invokes aggregation function.
     *
     * @param resolver A function used to resolve operands.
     * @param args     Arguments of the function.
     * @return Function result.
     * @throws IllegalArgumentException Unsupported input value.
     * @throws IllegalStateException    Unresolved operand.
     */
    @Override
    public Double invoke(final NameResolver resolver, final Object... args) throws Exception {
        return args.length > 0 && args[0] instanceof Number ? invoke(resolver, (Number) args[0]) : getFallbackValue();
    }
}
