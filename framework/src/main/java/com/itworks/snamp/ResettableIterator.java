package com.itworks.snamp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * Represents an iterator with reset support.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ResettableIterator<T> implements Iterator<T> {
    private final Iterable<T> iterable;
    private Iterator<T> iterator;

    /**
     * Initializes a new resettable iterator for the specified collection.
     * @param iterable
     */
    public ResettableIterator(final Iterable<T> iterable){
        this.iterable = Objects.requireNonNull(iterable);
        iterator = iterable.iterator();
    }

    public void reset(){
        iterator = iterable.iterator();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    @Override
    public T next() {
        return iterator.next();
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator (optional operation).  This method can be called
     * only once per call to {@link #next}.  The behavior of an iterator
     * is unspecified if the underlying collection is modified while the
     * iteration is in progress in any way other than by calling this
     * method.
     *
     * @throws UnsupportedOperationException if the {@code remove}
     *                                       operation is not supported by this iterator
     * @throws IllegalStateException         if the {@code next} method has not
     *                                       yet been called, or the {@code remove} method has already
     *                                       been called after the last call to the {@code next}
     *                                       method
     */
    @Override
    public void remove() {
        iterator.remove();
    }
}
