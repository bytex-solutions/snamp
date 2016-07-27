package com.bytex.snamp;

import com.google.common.collect.ObjectArrays;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents a list of weak references to event listeners.
 * <p>
 *    This class works like {@link java.util.concurrent.CopyOnWriteArrayList}.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@ThreadSafe
public abstract class WeakEventListenerList<L extends EventListener, E extends EventObject> implements Collection<L> {

    private static final WeakEventListener[] EMPTY_LISTENERS = new WeakEventListener[0];
    private volatile WeakEventListener<L, E>[] listeners;

    /**
     * Initializes a new empty list.
     */
    protected WeakEventListenerList(){
        listeners = EMPTY_LISTENERS;
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Unless otherwise specified by the implementing class,
     * actions are performed in the order of iteration (if an iteration order
     * is specified).  Exceptions thrown by the action are relayed to the
     * caller.
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     * @since 1.2
     */
    @Override
    public final void forEach(final Consumer<? super L> action) {
        final WeakEventListener<L, E>[] snapshot = listeners;
        for (final WeakEventListener<L, E> listenerRef : snapshot) {
            final L listener = listenerRef.get();
            if (listener != null)
                action.accept(listener);
        }
    }

    /**
     * Removes all of the elements of this collection that satisfy the given
     * predicate.  Errors or runtime exceptions thrown during iteration or by
     * the predicate are relayed to the caller.
     *
     * @param filter a predicate which returns {@code true} for elements to be
     *               removed
     * @return {@code true} if any elements were removed
     * @throws NullPointerException          if the specified filter is null
     * @throws UnsupportedOperationException if elements cannot be removed
     *                                       from this collection.  Implementations may throw this exception if a
     *                                       matching element cannot be removed or if, in general, removal is not
     *                                       supported.
     * @since 1.2
     */
    @Override
    public final synchronized boolean removeIf(final Predicate<? super L> filter) {
        @SuppressWarnings("unchecked")
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length];
        boolean result = false;
        int outputIndex = 0;
        //remove dead references or specified listener
        for (final WeakEventListener<L, E> listenerRef : listeners) {
            final L l = listenerRef.get();
            if (!result && filter.test(l))
                result = true;
            else if (l != null)
                newSnapshot[outputIndex++] = listenerRef;
        }
        this.listeners = outputIndex == 0 ? EMPTY_LISTENERS : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    /**
     * Gets count of listeners in this collection.
     * @return Count of listeners in this collection.
     */
    @Override
    public final int size(){
        return listeners.length;
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    @Override
    public final boolean isEmpty() {
        return ArrayUtils.isNullOrEmpty(listeners);
    }

    /**
     * Creates a new weak reference to the event listener.
     * @param listener A listener to be wrapped into weak reference. Cannot be {@literal null}.
     * @return A weak reference to the event listener.
     * @since 1.2
     */
    protected abstract WeakEventListener<L, E> createWeakEventListener(final L listener);

    /**
     * Adds a new weak reference to the specified listener.
     * @param listener An event listener. Cannot be {@literal null}.
     * @return Always {@literal true}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public final synchronized boolean add(final L listener) {
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length + 1];
        int outputIndex = 0;
        //remove dead references
        for(final WeakEventListener<L, E> listenerRef: listeners)
            if (listenerRef.get() != null)
                newSnapshot[outputIndex++] = listenerRef;

        //insert new element into the end of list
        newSnapshot[outputIndex++] = createWeakEventListener(listener);
        this.listeners = Arrays.copyOf(newSnapshot, outputIndex);
        return true;
    }

    /**
     * Removes the listener from this list.
     * @param listener A listener to remove. Cannot be {@literal null}.
     * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean remove(final Object listener) {
        return removeIf(listener::equals);
    }

    @Override
    public final boolean contains(final Object listener) {
        final WeakEventListener<?, ?>[] snapshot = listeners;
        for (final WeakEventListener<?, ?> listenerRef : snapshot)
            if (Objects.equals(listener, listenerRef.get()))
                return true;
        return false;
    }

    /**
     * Returns an array containing all of the elements in this collection.
     * If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     * <p/>
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this collection.  (In other words, this method must
     * allocate a new array even if this collection is backed by an array).
     * The caller is thus free to modify the returned array.
     * <p/>
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this collection
     */
    @Override
    public final EventListener[] toArray() {
        final WeakEventListener<L, ?>[] snapshot = listeners;
        final EventListener[] result = new EventListener[snapshot.length];
        int outputIndex = 0;
        for (final WeakEventListener<L, ?> listenerRef : snapshot) {
            final L listener = listenerRef.get();
            if (listener != null) result[outputIndex++] = listener;
        }
        return Arrays.copyOf(result, outputIndex);
    }

    @Override
    public final  <T> T[] toArray(T[] a) {
        final WeakEventListener<L, ?>[] snapshot = listeners;
        switch (a.length) {
            default:
                for (int inputIndex = 0, outputIndex = 0; inputIndex < Math.min(snapshot.length, a.length); inputIndex++) {
                    final L listener = snapshot[inputIndex].get();
                    if (listener != null) Array.set(a, outputIndex++, listener);
                }
                break;
            case 0:
                a = ObjectArrays.newArray(a, snapshot.length);
                int outputIndex = 0;
                for (final WeakEventListener<L, ?> listenerRef : snapshot) {
                    final L listener = listenerRef.get();
                    if (listener != null) Array.set(a, outputIndex++, listener);
                }
                break;
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized final boolean addAll(final Collection<? extends L> c) {
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length + c.size()];
        int outputIndex = 0;
        //remove dead references
        for (final WeakEventListener<L, E> listenerRef : listeners)
            if (listenerRef.get() != null)
                newSnapshot[outputIndex++] = listenerRef;

        //insert new elements into the end of list
        for (final L listener : c)
            newSnapshot[outputIndex++] = createWeakEventListener(listener);

        this.listeners = Arrays.copyOf(newSnapshot, outputIndex);
        return true;
    }

    @Override
    public synchronized final boolean removeAll(final Collection<?> c) {
        @SuppressWarnings("unchecked")
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length];
        boolean result = false;
        int outputIndex = 0;
        //remove dead references or specified listener
        for (final WeakEventListener<L, E> listenerRef : listeners) {
            final L l = listenerRef.get();
            if (c.contains(l))
                result = true;
            else if (l != null)
                newSnapshot[outputIndex++] = listenerRef;
        }
        this.listeners = outputIndex == 0 ? EMPTY_LISTENERS : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    @Override
    public synchronized final boolean retainAll(final Collection<?> c) {
        @SuppressWarnings("unchecked")
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length];
        boolean result = false;
        int outputIndex = 0;
        //remove dead references or specified listener
        for (final WeakEventListener<L, E> listenerRef : listeners) {
            final L l = listenerRef.get();
            if (c.contains(l))
                newSnapshot[outputIndex++] = listenerRef;
            else result |= l != null;
        }
        this.listeners = outputIndex == 0 ? EMPTY_LISTENERS : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    @Override
    public final boolean containsAll(final Collection<?> c) {
        final WeakEventListener<L, ?>[] snapshot = listeners;
        if (snapshot.length == 0) return c.size() == 0;
        int matched = 0;
        for (final WeakEventListener<L, ?> listenerRef : snapshot)
            for (final Object listener : c)
                if (Objects.equals(listenerRef.get(), listener))
                    matched++;
        return matched >= c.size();
    }

    /**
     * Passes event object to all listeners in this list.
     * @param event An event object.
     */
    public final void fire(final E event) {
        final WeakEventListener<L, E>[] snapshot = this.listeners;
        for (final WeakEventListener<L, E> listenerRef : snapshot)
            listenerRef.invoke(event);
    }

    /**
     * Passes event object to all listeners in parallel manner.
     * @param event An event object.
     * @since 1.2
     */
    public final void fireAsync(final E event) {
        final WeakEventListener<L, E>[] snapshot = this.listeners;
        Arrays.stream(snapshot).parallel().forEach(listener -> listener.invoke(event));
    }

    /**
     * Removes all listeners from this list.
     */
    @Override
    public final synchronized void clear() {
        for (int index = 0; index < listeners.length; index++) {
            final WeakEventListener<?, ?> listenerRef = listeners[index];
            listeners[index] = null;
            listenerRef.clear(); //help GC
        }
        listeners = EMPTY_LISTENERS;
    }

    private Stream<L> stream(final boolean parallel) {
        final WeakEventListener<L, E>[] snapshot = listeners;
        switch (snapshot.length) {
            default:
                return StreamSupport.stream(Arrays.spliterator(snapshot), parallel).map(WeakEventListener::get).filter(Objects::nonNull);
            case 0:
                return Stream.empty();
            case 1:
                final L listener = snapshot[0].get();
                return listener == null ? Stream.empty() : Stream.of(listener);
        }
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @since 1.2
     */
    @Override
    public final Stream<L> stream() {
        return stream(false);
    }

    /**
     * Returns a possibly parallel {@code Stream} with this collection as its
     * source.  It is allowable for this method to return a sequential stream.
     *
     * @return a possibly parallel {@code Stream} over the elements in this
     * collection
     * @since 1.2
     */
    @Override
    public final Stream<L> parallelStream() {
        return stream(true);
    }

    /**
     * Creates a {@link Spliterator} over the elements in this collection.
     *
     * @return a {@code Spliterator} over the elements in this collection
     * @since 1.2
     */
    @Override
    public final Spliterator<L> spliterator() {
        return stream().spliterator();
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public final Iterator<L> iterator() {
        final WeakEventListener<L, ?>[] snapshot = listeners;
        return snapshot.length == 0 ?
                ResettableIterator.of() :
                Arrays.stream(snapshot).map(WeakEventListener::get).filter(Objects::nonNull).iterator();
    }

    @Override
    public String toString() {
        final WeakEventListener<?, ?>[] snapshot = listeners;
        return snapshot == null ? "[]" : Arrays.toString(snapshot);
    }

    public static <L extends EventListener, E extends EventObject> WeakEventListenerList<L, E> create(final BiConsumer<? super L, ? super E> listenerInvoker){
        return new WeakEventListenerList<L, E>() {
            @Override
            protected WeakEventListener<L, E> createWeakEventListener(final L listener) {
                return WeakEventListener.create(listener, listenerInvoker);
            }
        };
    }
}
