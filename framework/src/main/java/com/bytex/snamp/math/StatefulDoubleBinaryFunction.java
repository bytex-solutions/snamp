package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;

import java.util.function.DoubleBinaryOperator;

/**
 * Represents f(x, y) that with Dom(f) = (double X double) and Cod(f) = double.
 */
public interface StatefulDoubleBinaryFunction extends DoubleBinaryOperator, Stateful {
}
