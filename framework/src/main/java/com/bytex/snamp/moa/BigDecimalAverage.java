package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface BigDecimalAverage extends DoubleConsumer, Consumer<BigDecimal>, Stateful, Cloneable, Serializable, Supplier<BigDecimal> {
}
