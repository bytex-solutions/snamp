package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;

import java.util.function.DoubleUnaryOperator;

/**
 * Represents f(x) that with Dom(f) = double and Cod(f) = double.
 */
public interface StatefulDoubleUnaryFunction extends DoubleUnaryOperator, Stateful {
}
