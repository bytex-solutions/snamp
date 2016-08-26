package com.bytex.snamp.connector.composite.functions;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.Objects;
import java.util.function.DoubleBinaryOperator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ToDoubleUnaryFunction extends ToDoubleFunction {
    private final AtomicDouble value;
    private final DoubleBinaryOperator operator;

    private ToDoubleUnaryFunction(final double initialValue, final DoubleBinaryOperator operator){
        this.operator = Objects.requireNonNull(operator);
        this.value = new AtomicDouble(initialValue);
    }

    @Override
    double compute(final double input) {
        double current, newValue;
        do {
            newValue = operator.applyAsDouble(current = value.get(), input);
        } while (!(value.compareAndSet(current, newValue)));
        return newValue;
    }

    static ToDoubleUnaryFunction min(){
        return new ToDoubleUnaryFunction(Double.MAX_VALUE, Math::min);
    }

    static ToDoubleUnaryFunction max(){
        return new ToDoubleUnaryFunction(Double.MIN_VALUE, Math::max);
    }

    static ToDoubleUnaryFunction sum(){
        return new ToDoubleUnaryFunction(0, (x, y) -> x + y);
    }
}
