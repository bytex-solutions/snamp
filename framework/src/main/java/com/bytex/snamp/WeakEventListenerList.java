package com.bytex.snamp;

import com.google.common.collect.ObjectArrays;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;

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
        listeners = null;
    }

    /**
     * Gets count of listeners in this collection.
     * @return Count of listeners in this collection.
     */
    @Override
    public final int size(){
        final EventListener[] snapshot = listeners;
        return snapshot == null ? 0 : snapshot.length;
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
        if(listeners == null) listeners = EMPTY_LISTENERS;
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
    public final synchronized boolean remove(final Object listener) {
        if (listeners == null) return false;
        @SuppressWarnings("unchecked")
        final WeakEventListener<L, E>[] newSnapshot = new WeakEventListener[listeners.length];
        boolean result = false;
        int outputIndex = 0;
        //remove dead references or specified listener
        for (final WeakEventListener<L, E> listenerRef : listeners) {
            final L l = listenerRef.get();
            if (!result && Objects.equals(l, listener))
                result = true;
            else if (l != null)
                newSnapshot[outputIndex++] = listenerRef;
        }
        this.listeners = outputIndex == 0 ? null : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    @Override
    public final boolean contains(final Object listener) {
        final WeakEventListener<?, ?>[] snapshot = listeners;
        if (snapshot == null) return false;
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
        if (snapshot == null) return EMPTY_LISTENERS;
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
        if(snapshot == null) return a;
        switch (a.length){
            default:
                for(int inputIndex = 0, outputIndex = 0; inputIndex < Math.min(snapshot.length, a.length); inputIndex++){
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
        if (listeners == null) listeners = EMPTY_LISTENERS;
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
        if (listeners == null) return false;
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
        this.listeners = outputIndex == 0 ? null : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    @Override
    public synchronized final boolean retainAll(final Collection<?> c) {
        if (listeners == null) return false;
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
        this.listeners = outputIndex == 0 ? null : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    @Override
    public final boolean containsAll(final Collection<?> c) {
        final WeakEventListener<L, ?>[] snapshot = listeners;
        if (snapshot == null) return c.size() == 0;
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
        if (snapshot != null)
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
        if (snapshot != null)
            Arrays.stream(snapshot).parallel().forEach(listener -> listener.invoke(event));
    }

    /**
     * Removes all listeners from this list.
     */
    @Override
    public final synchronized void clear() {
        if (listeners == null) return;
        for (int index = 0; index < listeners.length; index++) {
            final WeakEventListener<?, ?> listenerRef = listeners[index];
            listeners[index] = null;
            listenerRef.clear(); //help GC
        }
        listeners = null;
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public final Iterator<L> iterator() {
        final WeakEventListener<L, ?>[] snapshot = listeners;
        return snapshot == null ?
                ResettableIterator.of() :
                Arrays.stream(snapshot).map(WeakEventListener::get).filter(listener -> listener != null).iterator();
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
