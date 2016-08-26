package com.bytex.snamp.math;

/**
 * Represents a set of unary functions.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public final class UnaryFunctions {
    private UnaryFunctions(){
        throw new InstantiationError();
    }

    public static StatefulDoubleUnaryFunction percentile(final int capacity, final float percentile){
        return new PercentileFunction(capacity, percentile);
    }

    public static StatefulDoubleUnaryFunction sum(){
        return new StatefulDoubleUnaryFunction() {
            private double value;

            @Override
            public void reset() {
                value = 0;
            }

            @Override
            public double applyAsDouble(final double operand) {
                return value += operand;
            }
        };
    }

    /**
     * Represents function that computes average of input stream.
     * @return A new instance of function in its initial state.
     */
    public static StatefulDoubleUnaryFunction average() {
        return new StatefulDoubleUnaryFunction() {
            private long count = 0;
            private double sum = 0.0;

            @Override
            public void reset() {
                count = 0;
                sum = 0.0;
            }

            @Override
            public double applyAsDouble(final double operand) {
                count += 1;
                sum += operand;
                //prevent overflow
                if (count == Long.MAX_VALUE) {
                    count /= 2;
                    sum /= 2;
                } else if (Double.isInfinite(sum)) {
                    count = 1;
                    sum = operand;
                }
                return sum / count;
            }
        };
    }
}
