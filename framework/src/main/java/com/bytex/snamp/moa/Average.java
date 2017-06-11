package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import javax.annotation.Nonnull;
import java.util.function.DoubleConsumer;

/**
 * Represents abstract class for all average computations.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class Average extends Number implements DoubleConsumer, Stateful, Cloneable {
    private static final long serialVersionUID = -7033204722759946995L;

    @Override
    @Nonnull
    public abstract Average clone();
    
    /**
     * Computes average value.
     * @return Average value.
     */
    @Override
    public abstract double doubleValue();

    @Override
    public int intValue() {
        return Math.round(floatValue());
    }

    @Override
    public long longValue() {
        return Math.round(doubleValue());
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    @Override
    public String toString() {
        return Double.toString(doubleValue());
    }
}
