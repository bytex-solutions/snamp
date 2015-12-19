package com.bytex.snamp;

import java.util.*;

/**
 * Represents a list of weak references to event listeners.
 * <p>
 *    This class works like {@link java.util.concurrent.CopyOnWriteArrayList}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe
public abstract class WeakEventListenerList<L extends EventListener, E extends EventObject> implements Iterable<L> {
    private volatile WeakEventListener<L>[] listeners;

    /**
     * Initializes a new empty list.
     */
    @SuppressWarnings("unchecked")
    protected WeakEventListenerList(){
        listeners = null;
    }

    public final int size(){
        final WeakEventListener<L>[] snapshot = listeners;
        return snapshot == null ? 0 : snapshot.length;
    }

    /**
     * Adds a new weak reference to the specified listener.
     * @param listener An event listener. Cannot be {@literal null}.
     */
    @SuppressWarnings("unchecked")
    public final synchronized void add(final L listener) {
        if(listeners == null)
            listeners = ArrayUtils.emptyArray(WeakEventListener[].class);
        @SuppressWarnings("unchecked")
        final WeakEventListener<L>[] newSnapshot = new WeakEventListener[listeners.length + 1];
        int outputIndex = 0;
        //remove dead references
        for(final WeakEventListener<L> listenerRef: listeners)
            if (listenerRef.get() != null)
                newSnapshot[outputIndex++] = listenerRef;

        //insert new element into the end of list
        newSnapshot[outputIndex++] = new WeakEventListener<>(listener);

        this.listeners = Arrays.copyOf(newSnapshot, outputIndex);
    }

    /**
     * Removes the listener from this list.
     * @param listener A listener to remove. Cannot be {@literal null}.
     * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
     */
    public final synchronized boolean remove(final L listener) {
        if (listeners == null) return false;
        @SuppressWarnings("unchecked")
        final WeakEventListener<L>[] newSnapshot = new WeakEventListener[listeners.length];
        boolean result = false;
        int outputIndex = 0;
        //remove dead references or specified listener
        for (final WeakEventListener<L> listenerRef : listeners) {
            final L l = listenerRef.get();
            if (!result && Objects.equals(l, listener))
                result = true;
            else if (l != null)
                newSnapshot[outputIndex++] = listenerRef;
        }
        this.listeners = outputIndex == 0 ? null : Arrays.copyOf(newSnapshot, outputIndex);
        return result;
    }

    /**
     * Invokes the specified listener.
     * @param event An object to be passed into listener.
     * @param listener A listener to invoke.
     */
    protected abstract void invoke(final E event, final L listener);

    /**
     * Passes event object to all listeners in this list.
     * @param event An event object.
     */
    public final void fire(final E event) {
        final WeakEventListener<L>[] snapshot = this.listeners;
        if (snapshot != null)
            for (final WeakEventListener<L> listenerRef : snapshot) {
                final L listener = listenerRef.get();
                if (listener != null) invoke(event, listener);
            }
    }

    /**
     * Removes all listeners from this list.
     */
    @SuppressWarnings("unchecked")
    public final synchronized void clear() {
        for (int index = 0; index <= listeners.length; index++) {
            final WeakEventListener<L> listenerRef = listeners[index];
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
        final WeakEventListener<L>[] snapshot = listeners;
        if(snapshot == null) return ResettableIterator.of();
        final Collection<L> result = new LinkedList<>();
        for (final WeakEventListener<L> listenerRef : snapshot) {
            final L listener = listenerRef.get();
            if (listener != null)
                result.add(listener);
        }
        return result.iterator();
    }
}
