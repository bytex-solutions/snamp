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
    NumericFunction(final String... operands){
        super(SimpleType.DOUBLE, operands);
    }

    /**
     * Detects valid input type for this function.
     *
     * @param inputType Input type to check.
     * @return {@literal true}, if this function can accept a value of the specified type; otherwise, {@literal false}.
     */
    @Override
    public final boolean canAccept(final OpenType<?> inputType) {
        final WellKnownType type = WellKnownType.getType(inputType);
        return type != null && type.isNumber();
    }

    abstract double compute(final Number input, final OperandResolver resolver) throws Exception;

    @Override
    public final Double compute(final Object input, final OperandResolver resolver) throws Exception {
        if(input instanceof Number)
            return compute(((Number)input).doubleValue(), resolver);
        else
            throw new IllegalArgumentException(String.format("Incorrect function argument '%s'", input));
    }
}
