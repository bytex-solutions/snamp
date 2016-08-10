package com.bytex.snamp.connector.mda.impl.measurement;

import com.bytex.snamp.Localizable;

import javax.management.openmbean.OpenType;
import java.util.Set;
import java.util.SortedSet;

/**
 * Represents information about axis.
 * @param <V> Type of values in the axis
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Axis<V extends Comparable<V>> {
    enum Option{
        MIN_VALUE,
        MAX_VALUE,
        LEGAL_VALUES
    }

    /**
     * Gets unit of measurement.
     * @return Unit of measurement.
     */
    Localizable getUnit();

    boolean hasOption(final Option opt);

    V getMinValue() throws UnsupportedOperationException;

    V getMaxValue() throws UnsupportedOperationException;

    /**
     * Gets predefined set of values.
     * @return Immutable set of predefined values. If result is instance of {@link SortedSet} then values are sorted in natural order; {@link Set} for unsorted collection
     * @throws UnsupportedOperationException Predefined values are not supported by this axis.
     */
    Set<? extends V> getValues() throws UnsupportedOperationException;

    /**
     * Gets type of values in this axis.
     * @return Type of values in this axis.
     */
    OpenType<V> getType();
}
