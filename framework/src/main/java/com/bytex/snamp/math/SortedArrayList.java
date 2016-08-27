package com.bytex.snamp.math;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

/**
 * Represents sorted list of elements.
 * @since 2.0
 * @version 2.0
 */
class SortedArrayList<T> extends ArrayList<T> {
    private static final long serialVersionUID = -2304623947524466011L;
    private final Comparator<T> comparator;

    SortedArrayList(final int capacity, final Comparator<T> cmp){
        super(capacity);
        this.comparator = Objects.requireNonNull(cmp);
    }

    SortedArrayList(final Comparator<T> cmp){
        this(10, cmp);
    }

    private int computeIndex(final T item){
        int low = 0;
        int high = size() - 1;
        while (low <= high) {
            final int midIndex = (high + low) >>> 1;   //(high + low) / 2
            final T midValue = get(midIndex);
            final int comparisonResult = comparator.compare(item, midValue);
            if (comparisonResult > 0)    //input > midValue
                low = midIndex + 1;
            else if (comparisonResult < 0)
                high = midIndex - 1;
            else
                break;
        }
        return Math.max(low, high);
    }

    @Override
    public final boolean add(final T item) {
        add(computeIndex(item), item);
        return true;
    }

    public final void set(final T item){
        set(computeIndex(item), item);
    }
}
