package com.bytex.snamp;

import java.lang.ref.WeakReference;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Represents a weak reference to the event listener.
 * @param <L> Type of event listener.
 * @param <E> Type of event state object to be handled by listener.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public abstract class WeakEventListener<L extends EventListener, E extends EventObject> extends WeakReference<L> implements Supplier<L>, EventListener {
    private final int listenerHashCode;

    /**
     * Initializes a new weak reference to the event listener.
     * @param listener A listener to be wrapped into weak reference. Cannot be {@literal null}.
     */
    protected WeakEventListener(final L listener) {
        super(Objects.requireNonNull(listener));
        this.listenerHashCode = listener.hashCode();
    }

    /**
     * Invokes event listener and pass event state object into it.
     * @param event Event state object to be handled by listener.
     * @return {@literal true}, if reference to listener still alive; {@literal false}, if listener is garbage collected.
     */
    public final boolean invoke(final E event){
        final L listener = get();
        if(listener == null)
            return false;
        invoke(listener, event);
        return true;
    }

    /**
     * Invokes event listener and pass event state object into it.
     * @param listener A listener used to handle event. Cannot be {@literal null}.
     * @param event Event state object to be handled by listener.
     */
    protected abstract void invoke(final L listener, final E event);

    public static <L extends EventListener, E extends EventObject> WeakEventListener<L, E> create(final L listener, final BiConsumer<? super L, ? super E> handler){
        return new WeakEventListener<L, E>(listener) {
            @Override
            protected void invoke(final L listener, final E event) {
                handler.accept(listener, event);
            }
        };
    }

    private boolean equals(final WeakReference<?> other){
        return Objects.equals(get(), other.get());
    }

    @Override
    public final int hashCode() {
        return listenerHashCode ^ super.hashCode();
    }

    @Override
    public final boolean equals(final Object other) {
        return other instanceof WeakReference<?> && equals((WeakReference<?>) other);
    }

    @Override
    public String toString() {
        return Objects.toString(get(), "NoListener");
    }
}
