package com.bytex.snamp.moa;

import com.bytex.snamp.internal.Utils;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class RangeUtils {
    /**
     * Represents empty range of double values.
     */
    public static final Range<Double> EMPTY_DOUBLE_RANGE = Range.range(0D, BoundType.OPEN, 0D, BoundType.CLOSED);

    /**
     * Parses interval notation into range of double values.
     * @param intervalNotation Text in interval notation such as [42.0‥+∞)
     * @return Parser range.
     * @throws IllegalArgumentException Incorrect interval notation.
     */
    public static Range<Double> parseDoubleRange(final String intervalNotation) throws IllegalArgumentException {
        return Utils.callAndWrapException(() -> DoubleRangeParser.parse(intervalNotation), IllegalArgumentException::new);
    }

    /**
     * Gets relative location of the specified value.
     * @param value A value to check.
     * @param range A range.
     * @param <C> Type of values in range.
     * @return Zero, if the specified value is in range; -1 if the specified value lesser than lower endpoint of the range; 1 if the specified value greater than upper endpoint of the range.
     */
    public static <C extends Comparable<C>> int getLocation(final C value, final Range<C> range) {
        if (range.contains(value))
            return 0;
        else if (range.hasLowerBound()) {
            final C lowerEndpoint = range.lowerEndpoint();
            return value.compareTo(lowerEndpoint) <= 0 ? -1 : 1;
        } else
            return 1;
    }
}
