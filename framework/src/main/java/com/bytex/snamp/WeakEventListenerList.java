package com.bytex.snamp;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents collection of event listeners that holds weak reference to each listener.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public final class WeakEventListenerList<L extends EventListener, E extends EventObject> extends AbstractWeakEventListenerList<L, E> implements Collection<L> {
    private final Function<? super L, ? extends WeakEventListener<L, E>> listenerFactory;

    public WeakEventListenerList(@Nonnull final BiConsumer<? super L, ? super E> listenerInvoker){
        listenerFactory = createFactory(listenerInvoker);
    }

    private static <L extends EventListener, E extends EventObject> Function<? super L, ? extends WeakEventListener<L, E>> createFactory(final BiConsumer<? super L, ? super E> listenerInvoker){
        final class WeakEventListenerImpl extends WeakEventListener<L, E> {
            private WeakEventListenerImpl(@Nonnull final L listener) {
                super(listener);
            }

            @Override
            protected void invoke(@Nonnull final L listener, @Nonnull final E event) {
                listenerInvoker.accept(listener, event);
            }
        }
        
        return WeakEventListenerImpl::new;
    }

    @Override
    public boolean add(@Nonnull final L listener) {
        return add(listener, listenerFactory);
    }

    @Override
    public boolean addAll(@Nonnull final Collection<? extends L> c) {
        return addAll(c, listenerFactory);
    }
}
