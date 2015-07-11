package com.itworks.snamp.io;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.internal.annotations.SpecialUse;

import java.util.EventListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Represents named data bus for in-process communication.
 * This class cannot be inherited.
 * <p>
 *     This class cannot be used for inter-process communication.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class Communicator extends EventBus {
    private static final class IncomingMessageEvent extends SynchronizationEvent<Object> implements EventListener {
        private final Predicate<Object> responseFilter;

        private IncomingMessageEvent(final Predicate<Object> filter) {
            super(false);
            this.responseFilter = filter != null ? filter : Predicates.alwaysTrue();
        }

        @Subscribe
        @SpecialUse
        @AllowConcurrentEvents
        public void accept(final Object message) {
            if (responseFilter.apply(message))
                fire(message);
        }
    }

    //communication sessions
    private static final Cache<String, Communicator> communicators =
            CacheBuilder.newBuilder().weakValues().build();

    private Communicator() {
    }

    public static Communicator getSession(final String name) throws ExecutionException {
        return communicators.get(name, new Callable<Communicator>() {
            @Override
            public Communicator call() {
                return new Communicator();
            }
        });
    }

    private static Predicate<Object> exceptIncoming(final Object incomingMessage) {
        final int incomingIdentity = System.identityHashCode(incomingMessage);
        return new Predicate<Object>() {
            @Override
            public boolean apply(final Object actualMessage) {
                return incomingIdentity != System.identityHashCode(actualMessage);
            }
        };
    }

    public Object post(final Object message, final TimeSpan timeout) throws TimeoutException, InterruptedException {
        return post(message, null, timeout);
    }

    public Object post(final Object message, final long timeout) throws TimeoutException, InterruptedException {
        return post(message, null, timeout);
    }

    public Object post(final Object message, Predicate<Object> responseFilter, final TimeSpan timeout) throws TimeoutException, InterruptedException {
        responseFilter = Predicates.and(exceptIncoming(message), responseFilter);
        final IncomingMessageEvent event = new IncomingMessageEvent(responseFilter);
        final IncomingMessageEvent.EventAwaitor<Object> awaitor = event.getAwaitor();
        register(event);
        post(message);
        try {
            return awaitor.await(timeout);
        } finally {
            unregister(event);
        }
    }

    public Object post(final Object message, final Predicate<Object> responseFilter, final long timeout) throws TimeoutException, InterruptedException {
        return post(message, responseFilter, new TimeSpan(timeout));
    }

    public SynchronizationEvent<?> registerMessageSynchronizer(final Object except) {
        return registerMessageSynchronizer(exceptIncoming(except));
    }

    public SynchronizationEvent<?> registerMessageSynchronizer(final Predicate<Object> responseFilter) {
        final IncomingMessageEvent event = new IncomingMessageEvent(responseFilter);
        register(event);
        return event;
    }
}
