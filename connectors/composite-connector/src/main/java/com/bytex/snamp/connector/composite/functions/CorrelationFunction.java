package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.SimpleType;

/**
 * Represents a reference to the foreign attribute.
 */
final class CorrelationFunction extends NumericFunction {
    private final StatefulDoubleBinaryFunction correlation;

    CorrelationFunction(final String secondSource){
        super(secondSource);
        correlation = BinaryFunctions.correlation();
    }

    @Override
    double compute(final Number input, final OperandResolver resolver) throws Exception {
        final double other = getOperand(0, SimpleType.DOUBLE, resolver);
        return correlation.applyAsDouble(input.doubleValue(), other);
    }
}
