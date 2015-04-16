package com.itworks.snamp;

import com.itworks.snamp.internal.annotations.ThreadSafe;

import java.lang.ref.Reference;
import java.util.*;

/**
 * Represents a list of weak references to event listeners.
 * <p>
 *     This class is not thread-safe.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe(false)
public abstract class WeakEventListenerList<L extends EventListener, E extends EventObject> extends LinkedList<WeakEventListener<L>> {
    private static final long serialVersionUID = -9139754747382955308L;

    /**
     * Initializes a new empty list.
     */
    protected WeakEventListenerList(){

    }

    /**
     * Adds a new weak reference to the specified listener.
     * @param listener An event listener. Cannot be {@literal null}.
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public final boolean add(final L listener) {
        //remove dead references
        final Iterator<? extends Reference<L>> listeners = iterator();
        while (listeners.hasNext()){
            final Reference<?> l = listeners.next();
            if(l.get() == null) listeners.remove();
        }
        //add a new weak reference to the listener
        return add(new WeakEventListener<>(listener));
    }

    /**
     * Removes the listener from this list.
     * @param listener A listener to remove. Cannot be {@literal null}.
     * @return {@literal true}, if listener is removed successfully; otherwise, {@literal false}.
     */
    public final boolean remove(final L listener){
        final Iterator<? extends Reference<L>> listeners = iterator();
        while (listeners.hasNext()){
            final Reference<L> ref = listeners.next();
            final L l = ref.get();
            if(l == null) listeners.remove(); //remove dead reference
            else if(Objects.equals(listener, l)){
                ref.clear();    //help GC
                listeners.remove();
                return true;
            }
        }
        return false;
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
    public final void fire(final E event){
        final Iterator<? extends Reference<L>> listeners = iterator();
        while (listeners.hasNext()){
            final Reference<L> ref = listeners.next();
            final L l = ref.get();
            if(l == null) listeners.remove(); //remove dead reference
            else invoke(event, l);
        }
    }

    /**
     * Removes all listeners from this list.
     */
    @Override
    public final void clear() {
        final Iterator<? extends Reference<L>> refs = iterator();
        while (refs.hasNext()){
            refs.next().clear();    //help GC
            refs.remove();
        }
    }
}
