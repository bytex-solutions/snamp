package com.snamp;

import java.util.Objects;

/**
 * Represents range of values.
 * @author roman
 */
public class Range<T extends Comparable<T>> {

    /**
     * Represents inclusion test type.
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

        public final <T extends Comparable<T>> boolean isInRange(final T lowerBound, final T upperBound, final T value){
            if(includeLowerBound)
                return includeUpperBound ?
                        lowerBound.compareTo(value) >= 0 && upperBound.compareTo(value) <= 0 :
                        lowerBound.compareTo(value) >= 0 && upperBound.compareTo(value) < 0;
            else return includeUpperBound ?
                    lowerBound.compareTo(value) > 0 && upperBound.compareTo(value) <= 0 :
                    lowerBound.compareTo(value) > 0 && upperBound.compareTo(value) < 0;
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
     * @param lowerBound
     * @param upperBound
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
     * @param otherRange
     */
    public Range(final Range<? extends T> otherRange){
        this(otherRange.lowerBound, otherRange.upperBound);
    }

    public final Range<T> setUpperBound(final T upperBound){
        return new Range<>(lowerBound, upperBound);
    }

    public final Range<T> setLowerBound(final T lowerBound){
        return new Range<>(lowerBound, upperBound);
    }

    public final boolean contains(final T value, final InclusionTestType testType){
        return testType.isInRange(lowerBound, upperBound, value);
    }
}
