package com.bytex.snamp.connector.composite.functions;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class NumericUnaryFunction extends NumericFunction {
    private final AtomicDouble value;
    private final DoubleBinaryOperator operator;

    private NumericUnaryFunction(final double initialValue, final DoubleBinaryOperator operator){
        this.operator = Objects.requireNonNull(operator);
        this.value = new AtomicDouble(initialValue);
    }

    @Override
    double invoke(final NameResolver resolver, final Number input) {
        double current, newValue;
        do {
            newValue = operator.applyAsDouble(current = value.get(), input.doubleValue());
        } while (!(value.compareAndSet(current, newValue)));
        return newValue;
    }

    static NumericUnaryFunction min(){
        return new NumericUnaryFunction(Double.MAX_VALUE, Math::min);
    }

    static NumericUnaryFunction max(){
        return new NumericUnaryFunction(Double.MIN_VALUE, Math::max);
    }
}
