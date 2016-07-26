package com.bytex.snamp;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.EventListener;
import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class WeakEventListenerListTest extends Assert {
    private interface DummyListener extends EventListener{
        void invoke();
    }

    private static final class DummyEventListenerList extends WeakEventListenerList<DummyListener, EventObject>{
        @Override
        protected WeakEventListener<DummyListener, EventObject> createWeakEventListener(final DummyListener listener) {
            return WeakEventListener.create(listener, (l, e) -> l.invoke());
        }
    }

    @Test
    public void toArrayTest(){
        final DummyEventListenerList listeners = new DummyEventListenerList();
        final DummyListener listener = () -> {
        };
        listeners.addAll(ImmutableList.of(listener, listener));
        assertEquals(2, listeners.size());
        EventListener[] array = listeners.toArray();
        assertEquals(2, array.length);
    }

    @Test
    public void toArray2Test(){
        final DummyEventListenerList listeners = new DummyEventListenerList();
        final DummyListener listener = () -> {
        };
        listeners.addAll(ImmutableList.of(listener, listener));
        assertEquals(2, listeners.size());
        final DummyListener[] array = listeners.toArray(new DummyListener[3]);
        assertEquals(listener, array[0]);
        assertEquals(listener, array[1]);
        assertEquals(null, array[2]);
    }

    @Test
    public void addRemoveTest() {
        final DummyEventListenerList listeners = new DummyEventListenerList();
        final DummyListener listener = () -> {};
        listeners.add(listener);
        listeners.add(listener);
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(listener));
        for (final DummyListener l : listeners)
            assertEquals(System.identityHashCode(listener), System.identityHashCode(l));
        assertTrue(listeners.remove(listener));
        assertEquals(1, listeners.size());
        assertTrue(listeners.remove(listener));
        assertEquals(0, listeners.size());
        assertFalse(listeners.remove(listener));
    }

    @Test
    public void fireTest(){
        final DummyEventListenerList listeners = new DummyEventListenerList();
        final Box<Boolean> fired = new Box<>(Boolean.FALSE);
        final DummyListener listener = () -> fired.set(Boolean.TRUE);
        listeners.add(listener);
        assertTrue(listeners.containsAll(ImmutableList.of(listener)));
        listeners.fire(new EventObject(this));
        assertTrue(fired.get());
    }
}
