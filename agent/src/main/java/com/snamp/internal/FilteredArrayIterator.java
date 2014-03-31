package com.snamp.internal;

import com.snamp.internal.Internal;

import java.util.Iterator;

/**
 * Represents a base class for building filtered array iterator.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class FilteredArrayIterator<T> implements Iterator<T> {
    private final T[] array;
    private int currentPos;

    protected FilteredArrayIterator(final T[] array, final int startIndex){
        this.array = array;
        this.currentPos = startIndex;
    }

    protected FilteredArrayIterator(final T[] array){
        this(array, 0);
    }

    protected abstract boolean filter(final T element);

    /**
     * Returns the current position of this iterator.
     * @return The current position of this iterator.
     */
    public final int getCurrentPosition(){
        return currentPos;
    }

    /**
     * Determines whether the next value is available.
     * @return {@literal true}, if the end of iterator is not reached; otherwise, {@literal false}.
     */
    @Override
    public final boolean hasNext() {
        return currentPos < (array.length - 1);
    }

    /**
     * Returns the next array element.
     * @return The next array element.
     */
    @Override
    public T next() {
        final T currentValue = array[currentPos++];
        return filter(currentValue) ? currentValue : next();
    }

    /**
     * Operation is not supported.
     */
    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
