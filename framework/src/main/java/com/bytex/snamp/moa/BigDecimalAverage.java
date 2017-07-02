package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

/**
 * Represents common interface for average implementation based on {@link BigDecimal} value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface BigDecimalAverage extends DoubleConsumer, Consumer<BigDecimal>, Stateful, Cloneable, Serializable, Supplier<BigDecimal> {
}
