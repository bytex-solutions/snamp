package com.bytex.snamp;

import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ObjectArrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents an abstract class for building list of weak event listeners.
 * <p>
 *    This class works like {@link java.util.concurrent.CopyOnWriteArrayList}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public abstract class AbstractWeakEventListenerList<L extends EventListener, E extends EventObject> implements Iterable<L>, Consumer<E> {
    private volatile WeakEventListener<L, E>[] listeners;

    /**
     * Initializes a new empty list.
     */
    protected AbstractWeakEventListenerList(){
        listeners = emptyListeners();
    }

    @SuppressWarnings("unchecked")
    private static <L extends EventListener, E extends EventObject> WeakEventListener<L, E>[] emptyListeners(){
        return ArrayUtils.emptyArray(WeakEventListener[].class);
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
    public final void forEach(final Consumer<? super L> action) {
        final WeakEventListener<L, E>[] snapshot = listeners;
        for (final WeakEventListener<L, E> listenerRef : snapshot) {
            final L listener = listenerRef.get();
            if (listener != null)
                action.accept(listener);
        }
    }

    public final void parallelForEach(final Consumer<? super L> action, final Executor executor) {
        final WeakEventListener<L, E>[] snapshot = listeners;
        for (final WeakEventListener<L, E> listenerRef : snapshot) {
            final L listener = listenerRef.get();
            if (listener != null)
                executor.execute(() -> action.accept(listener));
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
    public final synchronized boolean removeIf(@Nonnull final Predicate<? super L> filter) {
        if(isEmpty())
            return false;
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
        this.listeners = outputIndex == 0 ? emptyListeners() : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    /**
     * Remove dead references to the listeners.
     * @return Number of survived listeners.
     */
    public final synchronized int sanitize() {
        if (isEmpty())
            return 0;
        @SuppressWarnings("unchecked")
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length];
        int outputIndex = 0;
        for (final WeakEventListener<L, E> listenerRef : listeners)
            if (listenerRef.get() != null)
                newSnapshot[outputIndex++] = listenerRef;
        this.listeners = outputIndex == 0 ? emptyListeners() : Arrays.copyOf(newSnapshot, outputIndex);
        return outputIndex;
    }

    /**
     * Gets count of listeners in this collection.
     * @return Count of listeners in this collection.
     */
    public final int size(){
        return listeners.length;
    }

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @return <tt>true</tt> if this collection contains no elements
     */
    public final boolean isEmpty() {
        return ArrayUtils.isNullOrEmpty(listeners);
    }

    /**
     * Adds a new weak reference to the specified listener.
     * @param listener An event listener to add. Cannot be {@literal null}.
     * @return Always {@literal true}.
     */
    @SuppressWarnings("unchecked")
    protected final synchronized boolean add(@Nonnull final WeakEventListener<L, E> listener) {
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length + 1];
        int outputIndex = 0;
        //remove dead references
        for(final WeakEventListener<L, E> listenerRef: listeners)
            if (listenerRef.get() != null)
                newSnapshot[outputIndex++] = listenerRef;

        //insert new element into the end of list
        newSnapshot[outputIndex++] = listener;
        this.listeners = Arrays.copyOf(newSnapshot, outputIndex);
        return true;
    }

    /**
     * Removes the listener from this list.
     * @param listener A listener to remove. Cannot be {@literal null}.
     * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
     */
    public final boolean remove(@Nonnull final Object listener) {
        return removeIf(listener::equals);
    }

    public final boolean contains(@Nonnull final Object listener) {
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

    @Nonnull
    public final  <T> T[] toArray(@Nonnull T[] a) {
        final WeakEventListener<L, ?>[] snapshot = listeners;
        switch (a.length) {
            default:
                for (int inputIndex = 0, outputIndex = 0; inputIndex < Integer.min(snapshot.length, a.length); inputIndex++) {
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
    protected synchronized final boolean addAll(@Nonnull final Collection<? extends L> c,
                                                @Nonnull final Function<? super L, ? extends WeakEventListener<L, E>> listenerFactory) {
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length + c.size()];
        int outputIndex = 0;
        //remove dead references
        for (final WeakEventListener<L, E> listenerRef : listeners)
            if (listenerRef.get() != null)
                newSnapshot[outputIndex++] = listenerRef;

        //insert new elements into the end of list
        for (final L listener : c)
            newSnapshot[outputIndex++] = listenerFactory.apply(listener);

        this.listeners = Arrays.copyOf(newSnapshot, outputIndex);
        return true;
    }

    public synchronized final boolean removeAll(@Nonnull final Collection<?> c) {
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
        this.listeners = outputIndex == 0 ? emptyListeners() : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    public synchronized final boolean retainAll(@Nonnull final Collection<?> c) {
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
        this.listeners = outputIndex == 0 ? emptyListeners() : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    public final boolean containsAll(@Nonnull final Collection<?> c) {
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
     * Alias for {@link #fire(EventObject)}.
     * @param event An event object.
     * @since 2.0
     */
    @Override
    public final void accept(final E event) {
        fire(event);
    }

    public final void fire(final Supplier<? extends E> eventSupplier){
        final WeakEventListener<L, E>[] snapshot = this.listeners;
        if(snapshot.length > 0) {
            final E event = eventSupplier.get();
            for (final WeakEventListener<L, E> listenerRef : snapshot)
                listenerRef.invoke(event);
        }
    }

    /**
     * Passes event object to all listeners in parallel manner.
     * @param event An event object.
     * @since 1.2
     */
    public final void fireAsync(final E event, final Executor executor) {
        final WeakEventListener<L, E>[] snapshot = this.listeners;
        //optimization cases
        switch (snapshot.length) {
            case 0:
                return;
            case 1:
            case 2:
            case 3:
            case 4:
                for (final WeakEventListener<?, E> listener : snapshot)
                    executor.execute(() -> listener.invoke(event));
            default:
                Utils.parallelForEach(Arrays.spliterator(snapshot), listener -> listener.invoke(event), executor);
        }
    }

    /**
     * Removes all listeners from this list.
     */
    public final synchronized void clear() {
        for (int index = 0; index < listeners.length; index++) {
            final WeakEventListener<?, ?> listenerRef = listeners[index];
            listeners[index] = null;
            listenerRef.clear(); //help GC
        }
        listeners = emptyListeners();
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
    @Nonnull
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
}
