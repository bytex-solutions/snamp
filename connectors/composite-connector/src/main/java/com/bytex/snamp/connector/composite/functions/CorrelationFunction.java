package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.math.Correlation;

import javax.management.openmbean.SimpleType;
import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

/**
 * Represents a reference to the foreign attribute.
 */
final class CorrelationFunction extends NumericFunction {
    private final String secondSource;
    private final DoubleBinaryOperator correlation;

    CorrelationFunction(final String secondSource){
        this.secondSource = Objects.requireNonNull(secondSource);
        this.correlation = new Correlation();
    }

    @Override
    double invoke(final NameResolver resolver, final Number input) throws Exception {
        final double other = resolver.resolveAs(secondSource, SimpleType.DOUBLE);
        return correlation.applyAsDouble(input.doubleValue(), other);
    }
}
