package com.bytex.snamp;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.EventListener;
import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class WeakEventListenerListTest extends Assert {
    private interface DummyListener extends EventListener{
        void invoke(final EventObject e);
    }

    @Test
    public void streamTest(){
        final WeakEventListenerList<DummyListener, EventObject> listeners = new WeakEventListenerList<>(DummyListener::invoke);
        final DummyListener listener = e -> {
        };
        listeners.add(listener);
        listeners.add(listener);
        final DummyListener[] array = listeners.toArray(new DummyListener[0]);
        assertEquals(2, array.length);
        assertEquals(listener, array[0]);
        assertEquals(listener, array[1]);
    }

    @Test
    public void toArrayTest(){
        final WeakEventListenerList<DummyListener, EventObject> listeners = new WeakEventListenerList<>(DummyListener::invoke);
        final DummyListener listener = e -> {
        };
        listeners.addAll(ImmutableList.of(listener, listener));
        assertEquals(2, listeners.size());
        EventListener[] array = listeners.toArray();
        assertEquals(2, array.length);
    }

    @Test
    public void toArray2Test(){
        final WeakEventListenerList<DummyListener, EventObject> listeners = new WeakEventListenerList<>(DummyListener::invoke);
        final DummyListener listener = e -> {
        };
        listeners.addAll(ImmutableList.of(listener, listener));
        assertEquals(2, listeners.size());
        final DummyListener[] array = listeners.toArray(new DummyListener[3]);
        assertEquals(listener, array[0]);
        assertEquals(listener, array[1]);
        assertNull(array[2]);
    }

    @Test
    public void addRemoveTest() {
        final WeakEventListenerList<DummyListener, EventObject> listeners = new WeakEventListenerList<>(DummyListener::invoke);
        final DummyListener listener = e -> {};
        listeners.add(listener);
        listeners.add(listener);
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(listener));
        for (final DummyListener l : listeners)
            assertEquals(System.identityHashCode(listener), System.identityHashCode(l));
        assertTrue(listeners.removeIf(listener::equals));
        assertEquals(1, listeners.size());
        assertTrue(listeners.remove(listener));
        assertEquals(0, listeners.size());
        assertFalse(listeners.remove(listener));
    }

    @Test
    public void fireTest(){
        final WeakEventListenerList<DummyListener, EventObject> listeners = new WeakEventListenerList<>(DummyListener::invoke);
        final BooleanBox fired = BooleanBox.of(false);
        final DummyListener listener = e -> fired.set(true);
        listeners.add(listener);
        assertTrue(listeners.containsAll(ImmutableList.of(listener)));
        listeners.fire(new EventObject(this));
        assertTrue(fired.get());
    }
}
