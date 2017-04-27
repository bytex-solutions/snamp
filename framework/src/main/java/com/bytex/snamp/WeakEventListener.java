package com.bytex.snamp;

import javax.annotation.Nonnull;
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
    private int hashCode;

    /**
     * Initializes a new weak reference to the event listener.
     * @param listener A listener to be wrapped into weak reference. Cannot be {@literal null}.
     */
    protected WeakEventListener(@Nonnull final L listener) {
        super(listener);
    }

    /**
     * Invokes event listener and pass event state object into it.
     * @param event Event state object to be handled by listener.
     * @return {@literal true}, if reference to listener still alive; {@literal false}, if listener is garbage collected.
     */
    public final boolean invoke(@Nonnull final E event){
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
    protected abstract void invoke(@Nonnull final L listener, @Nonnull final E event);

    public static <L extends EventListener, E extends EventObject> WeakEventListener<L, E> create(final L listener, final BiConsumer<? super L, ? super E> handler){
        return new WeakEventListener<L, E>(listener) {
            @Override
            protected void invoke(@Nonnull final L listener, @Nonnull final E event) {
                handler.accept(listener, event);
            }
        };
    }

    private boolean equals(final WeakReference<?> other){
        return Objects.equals(get(), other.get());
    }

    @Override
    public final int hashCode() {
        if (hashCode == 0)
            hashCode = Objects.hashCode(get());
        return hashCode;
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
