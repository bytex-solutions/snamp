package com.bytex.snamp;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents array enumerator.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe(false)
public final class ArrayEnumerator<T> implements Enumeration<T> {
    private final T[] array;
    private int currentIndex;

    /**
     * Initializes a new enumerator for the specified array.
     * @param array An array to wrap into the enumerator. Cannot be {@literal null}.
     */
    public ArrayEnumerator(final T[] array){
        this.array = Objects.requireNonNull(array);
        currentIndex = 0;
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return <code>true</code> if and only if this enumeration object
     * contains at least one more element to provide;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean hasMoreElements() {
        return currentIndex < array.length;
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return the next element of this enumeration.
     * @throws NoSuchElementException if no more elements exist.
     */
    @Override
    public T nextElement() throws NoSuchElementException{
        if(hasMoreElements())
            return array[currentIndex++];
        else throw new NoSuchElementException();
    }
}
