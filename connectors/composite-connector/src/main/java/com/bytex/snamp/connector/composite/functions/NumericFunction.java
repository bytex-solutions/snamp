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

    @Override
    public final boolean canAccept(final int index, final OpenType<?> inputType) {
        switch (index) {
            case 0:
                final WellKnownType type = WellKnownType.getType(inputType);
                return type != null && type.isNumber();
            default:
                return false;
        }
    }

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
        if (args.length > 0 && args[0] instanceof Number)
            return invoke(resolver, (Number) args[0]);
        else
            throw new IllegalArgumentException("The first argument is not a number");
    }
}
