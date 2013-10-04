package com.snamp;

import java.util.Iterator;

/**
 * Represents a base class for building filtered array iterator.
 * @author roman
 */
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
     * Returns the current position of the iterator.
     * @return
     */
    public final int getCurrentPosition(){
        return currentPos;
    }

    /**
     * Determines whether the next value
     * @return
     */
    @Override
    public final boolean hasNext() {
        return currentPos < (array.length - 1);
    }

    /**
     * Returns the next array element.
     * @return
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
