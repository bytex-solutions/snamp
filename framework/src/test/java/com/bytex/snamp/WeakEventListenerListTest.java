package com.bytex.snamp;

import org.junit.Assert;
import org.junit.Test;

import java.util.EventListener;
import java.util.EventObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WeakEventListenerListTest extends Assert {
    private interface DummyListener extends EventListener{
        void invoke();
    }

    private static final class DummyEventListenerList extends WeakEventListenerList<DummyListener, EventObject>{
        @Override
        protected void invoke(final EventObject event, final DummyListener listener) {
            listener.invoke();
        }
    }

    @Test
    public void addRemoveTest() {
        final DummyEventListenerList listeners = new DummyEventListenerList();
        final DummyListener listener = new DummyListener() {
            @Override
            public void invoke() {

            }
        };
        listeners.add(listener);
        listeners.add(listener);
        assertEquals(2, listeners.size());
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
        final DummyListener listener = new DummyListener() {
            @Override
            public void invoke() {
                fired.set(Boolean.TRUE);
            }
        };
        listeners.add(listener);
        listeners.fire(new EventObject(this));
        assertTrue(fired.get());
    }
}
