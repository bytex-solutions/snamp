package com.bytex.snamp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Represents an iterator with reset support.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@ThreadSafe(false)
public abstract class ResettableIterator<T> implements Iterator<T>, Serializable, Enumeration<T> {
    //iterator with zero elements
    private static final class EmptyIterator extends ResettableIterator{
        private static final EmptyIterator INSTANCE = new EmptyIterator();
        private static final long serialVersionUID = -7846749919844312382L;

        private EmptyIterator(){

        }

        @Override
        public void reset() {
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported for empty iterator.");
        }

        @SuppressWarnings("unchecked")
        private static <T> ResettableIterator<T> getInstance(){
            return INSTANCE;
        }
    }
    //abstract class for all array-based iterators
    private static abstract class ArrayIterator<T> extends ResettableIterator<T>{
        private static final long serialVersionUID = -6510072048310473619L;
        private int position = 0;

        protected abstract int getLength();

        protected abstract T get(final int index);

        @Override
        public final void reset() {
            position = 0;
        }

        @Override
        public final boolean hasNext() {
            return position < getLength();
        }

        @Override
        public final T next() {
            if(position < getLength())
                return get(position++);
            else throw new NoSuchElementException("End of array reached. Reset iterator to continue.");
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException("Array element cannot be removed");
        }

        @Override
        public abstract String toString();
    }

    private static final long serialVersionUID = -1555266472709155869L;

    /**
     * Initializes a new iterator with reset support.
     */
    protected ResettableIterator(){
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return <code>true</code> if and only if this enumeration object
     * contains at least one more element to provide;
     * <code>false</code> otherwise.
     */
    @Override
    public final boolean hasMoreElements() {
        return hasNext();
    }

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return the next element of this enumeration.
     * @throws NoSuchElementException if no more elements exist.
     */
    @Override
    public final T nextElement() {
        return next();
    }

    /**
     * Sets iterator to its initial position.
     */
    public abstract void reset();

    /**
     * Constructs iterator for the specified collection.
     * @param iterable A collection to wrap. Cannot be {@literal null}.
     * @param <T> Type of the element in the collection.
     * @return A new instance of resettable iterator.
     */
    public static <T> ResettableIterator<T> of(final Iterable<T> iterable){
        return new ResettableIterator<T>(){
            private static final long serialVersionUID = 8372425179207081157L;
            private Iterator<T> iterator = iterable.iterator();

            @Override
            public void reset() {
                iterator = iterable.iterator();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            @Override
            public String toString() {
                return iterable.toString();
            }
        };
    }

    /**
     * Obtains resettable iterator with single element.
     * @param supplier Supplier for the single element.
     * @param <T> Type of the element in the iterator.
     * @return Iterator with single element.
     */
    public static <T> ResettableIterator<T> of(final Supplier<? extends T> supplier){
        return new ResettableIterator<T>() {
            private static final long serialVersionUID = -4470259466640767812L;
            private boolean available = true;

            @Override
            public void reset() {
                available = true;
            }

            @Override
            public boolean hasNext() {
                return available;
            }

            @Override
            public T next() {
                if(available){
                    available = false;
                    return supplier.get();
                }
                else throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("This collection is singleton");
            }
        };
    }

    /**
     * Obtains resettable iterator with single element.
     * @param value An element in the iterator.
     * @param <T> Type of the element in the iterator.
     * @return Iterator with single element.
     */
    public static <T> ResettableIterator<T> of(final T value) {
        return of(() -> value);
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param items An array to wrap. Cannot be {@literal null}.
     * @param <T> Type of the array component.
     * @return A new iterator for the specified array.
     */
    @SafeVarargs
    public static <T> ResettableIterator<T> of(final T... items) {
        switch (items.length) {
            case 0:
                return EmptyIterator.getInstance();
            default:
                return new ArrayIterator<T>() {
                    private static final long serialVersionUID = -1849965276230507239L;

                    @Override
                    protected int getLength() {
                        return items.length;
                    }

                    @Override
                    protected T get(final int index) {
                        return items[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(items);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Byte> of(final byte[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Byte>() {
                    private static final long serialVersionUID = 4477058913151343101L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Byte get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Short> of(final short[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Short>() {
                    private static final long serialVersionUID = -2120797310172397170L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Short get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Integer> of(final int[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Integer>() {
                    private static final long serialVersionUID = -2120797310172397170L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Integer get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Long> of(final long[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Long>() {
                    private static final long serialVersionUID = -2120797310172397170L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Long get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Boolean> of(final boolean[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Boolean>() {
                    private static final long serialVersionUID = -2120797310172397170L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Boolean get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Float> of(final float[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Float>() {
                    private static final long serialVersionUID = -2120797310172397170L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Float get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Double> of(final double[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Double>() {
                    private static final long serialVersionUID = -2120797310172397170L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Double get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified array.
     * @param array An array to wrap. Cannot be {@literal null}.
     * @return A new iterator for the specified array.
     */
    public static ResettableIterator<Character> of(final char[] array) {
        switch (array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Character>() {
                    private static final long serialVersionUID = -2120797310172397170L;

                    @Override
                    protected int getLength() {
                        return array.length;
                    }

                    @Override
                    protected Character get(final int index) {
                        return array[index];
                    }

                    @Override
                    public String toString() {
                        return Arrays.toString(array);
                    }
                };
        }
    }

    /**
     * Obtains resettable iterator for the specified sequence of characters.
     * @param value A sequence of characters. Cannot be {@literal null}.
     * @return A new iterator for the specified sequence.
     */
    public static ResettableIterator<Character> of(final CharSequence value) {
        switch (value.length()) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Character>() {
                    private static final long serialVersionUID = 6565775008504686243L;

                    @Override
                    protected int getLength() {
                        return value.length();
                    }

                    @Override
                    protected Character get(final int index) {
                        return value.charAt(index);
                    }

                    @Override
                    public String toString() {
                        return value.toString();
                    }
                };
        }
    }
}