package com.bytex.snamp;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents an iterator with reset support.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@NotThreadSafe
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

        @Override
        public void forEachRemaining(final Consumer action) {

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
        private final int length;

        private ArrayIterator(final int length){
            this.length = length;
        }

        protected abstract T get(final int index);

        @Override
        public final void reset() {
            position = 0;
        }

        @Override
        public final boolean hasNext() {
            return position < length;
        }

        @Override
        public final T next() {
            if(position < length)
                return get(position++);
            else throw new NoSuchElementException("End of array reached. Reset iterator to continue.");
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException("Array element cannot be removed");
        }

        @Override
        public final void forEachRemaining(final Consumer<? super T> action) {
            while (position < length)
                action.accept(get(position++));
        }

        @Override
        public abstract String toString();
    }

    private static final long serialVersionUID = -1555266472709155869L;

    /**
     * Initializes a new iterator with reset support.
     */
    private ResettableIterator(){
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
        return of(iterable, false);
    }

    /**
     * Constructs iterator for the specified collection.
     * @param iterable A collection to wrap. Cannot be {@literal null}.
     * @param readOnly {@literal true}
     * @param <T> Type of the element in the collection.
     * @return A new instance of resettable iterator.
     * @since 2.1
     */
    public static <T> ResettableIterator<T> of(final Iterable<T> iterable, final boolean readOnly) {
        class DefaultIterator extends ResettableIterator<T> {
            private static final long serialVersionUID = 8372425179207081157L;
            private Iterator<T> iterator = iterable.iterator();

            @Override
            public final void reset() {
                iterator = iterable.iterator();
            }

            @Override
            public final boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            @Override
            public final T next() {
                return iterator.next();
            }

            @Override
            public final void forEachRemaining(final Consumer<? super T> action) {
                iterator.forEachRemaining(action);
            }

            @Override
            public String toString() {
                return iterable.toString();
            }
        }

        final class ReadOnlyIterator extends DefaultIterator{
            private static final long serialVersionUID = 1531437949993154268L;

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Iterator is read-only");
            }
        }

        return readOnly ? new ReadOnlyIterator() : new DefaultIterator();
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
        final int len;
        switch (len = items.length) {
            case 0:
                return EmptyIterator.getInstance();
            case 1:
                return of(items[0]);
            default:
                return new ArrayIterator<T>(len) {
                    private static final long serialVersionUID = -1849965276230507239L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Byte>(len) {
                    private static final long serialVersionUID = 4477058913151343101L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Short>(len) {
                    private static final long serialVersionUID = -2120797310172397170L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Integer>(len) {
                    private static final long serialVersionUID = -2120797310172397170L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Long>(len) {
                    private static final long serialVersionUID = -2120797310172397170L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Boolean>(len) {
                    private static final long serialVersionUID = -2120797310172397170L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Float>(len) {
                    private static final long serialVersionUID = -2120797310172397170L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Double>(len) {
                    private static final long serialVersionUID = -2120797310172397170L;

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
        final int len;
        switch (len = array.length) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Character>(len) {
                    private static final long serialVersionUID = -2120797310172397170L;

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
        final int len;
        switch (len = value.length()) {
            case 0:
                return of();
            default:
                return new ArrayIterator<Character>(len) {
                    private static final long serialVersionUID = 6565775008504686243L;

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