package com.bytex.snamp.connector.composite.functions;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Represents a reference to the foreign attribute.
 */
final class CorrelationFunction extends ToDoubleFunction {
    private final Callable<?> secondSource;

    CorrelationFunction(final Callable<?> secondSource){
        this.secondSource = Objects.requireNonNull(secondSource);
    }

    @Override
    double compute(double input) {
        return 0;
    }
}
