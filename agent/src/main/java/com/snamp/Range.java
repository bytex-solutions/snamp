package com.snamp;

import java.util.Objects;

/**
 * Represents range of values.<br/>
 * <p>
 *     <b>Example:</b><br/>
 *     <pre>{@code
 *     final Range<Integer> r = new Range<>(1, 10);
 *     final boolean b = r.contains(5, InclusionTestType.FULL_INCLUSIVE);
 *     }</pre>
 * </p>
 * @param <T> Type of the range elements.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public class Range<T extends Comparable<T>> {

    /**
     * Represents inclusion test type.
     * @since 1.0
     * @version 1.0
     */
    public static enum InclusionTestType{
        /**
         * [lower, upper]
         */
        FULL_INCLUSIVE(true, true),

        /**
         * (lower, upper)
         */
        FULL_EXCLUSIVE(false, false),

        /**
         * [lower, upper)
         */
        INCLUDE_LOWER_BOUND_EXCLUDE_UPPER_BOUND(true, false),

        /**
         * (lower, upper]
         */
        EXCLUDE_LOWER_BOUND_INCLUDE_UPPER_BOUND(false, true);

        /**
         * Determines whether the lower bound is included in comparison.
         */
        public final boolean includeLowerBound;

        /**
         * Determines whether the upper bound is included in comparison.
         */
        public final boolean includeUpperBound;

        private InclusionTestType(final boolean includeLowerBound, final boolean includeUpperBound){
            this.includeLowerBound = includeLowerBound;
            this.includeUpperBound = includeUpperBound;
        }

        /**
         * Tests whether the specified value is in specified range.
         * @param lowerBound The lower bound of the range.
         * @param upperBound The upper bound of the range.
         * @param value The value to test.
         * @param <T> Type of the range elements.
         * @return {@literal true}, if the specified value is in range; otherwise, {@literal false}.
         */
        public final <T extends Comparable<T>> boolean isInRange(final T lowerBound, final T upperBound, final T value){
            if(includeLowerBound)
                return includeUpperBound ?
                        lowerBound.compareTo(value) <= 0 && upperBound.compareTo(value) >= 0 :
                        lowerBound.compareTo(value) <= 0 && upperBound.compareTo(value) > 0;
            else return includeUpperBound ?
                    lowerBound.compareTo(value) < 0 && upperBound.compareTo(value) >= 0 :
                    lowerBound.compareTo(value) < 0 && upperBound.compareTo(value) > 0;
        }
    }

    /**
     * Represents lower bound of the range.
     */
    public final T lowerBound;

    /**
     * Represents upper bound of the range.
     */
    public final T upperBound;

    /**
     * Initializes a new range.
     * @param lowerBound The lower bound of the range.
     * @param upperBound The upper bound of the range.
     */
    public Range(final T lowerBound, final T upperBound){
        if(lowerBound == null) throw new IllegalArgumentException("lowerBound is null.");
        else if(upperBound == null) throw new IllegalArgumentException("upperBound is null.");
        if(lowerBound.compareTo(upperBound) > 0){
            this.lowerBound = upperBound;
            this.upperBound = lowerBound;
        }
        else {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }

    /**
     * Initializes a new range from the specified range.
     * @param otherRange The prototype range.
     */
    public Range(final Range<? extends T> otherRange){
        this(otherRange.lowerBound, otherRange.upperBound);
    }

    /**
     * Creates a new {@code Range} with the changed upper bound.
     * @param upperBound The new range upper bound.
     * @return A new instance of the {@code Range} with the specified upper bound.
     */
    public final Range<T> setUpperBound(final T upperBound){
        return new Range<>(lowerBound, upperBound);
    }

    /**
     * Creates a new {@code Range} with the changed lower bound.
     * @param lowerBound The new range lower bound.
     * @return A new instance of the {@code Range} with the specified lower bound.
     */
    public final Range<T> setLowerBound(final T lowerBound){
        return new Range<>(lowerBound, upperBound);
    }

    /**
     * Determines whether the specified value is in current range.
     * @param value The value to test.
     * @param testType Inclusion test strategy. Cannot be {@literal null}.
     * @return {@literal true}, if the specified value is in current range; otherwise, {@literal false}.
     */
    public final boolean contains(final T value, final InclusionTestType testType){
        return testType.isInRange(lowerBound, upperBound, value);
    }
}
