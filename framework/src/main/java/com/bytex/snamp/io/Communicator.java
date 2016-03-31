package com.bytex.snamp.io;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.SpecialUse;

import java.util.EventListener;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Represents named data bus for in-process communication.
 * This class cannot be inherited.
 * <p>
 *     This class cannot be used for inter-process communication.
 * @author Roman Sakno
 * @version 1.2
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
            boolean success;
            try{
                success = responseFilter.apply(message);
            }
            catch (final Throwable e){
                raise(e);
                success = false;
            }
            if(success)
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

    public Object post(final Object message, final TimeSpan timeout) throws TimeoutException, InterruptedException, ExecutionException {
        return post(message, null, timeout);
    }

    public Object post(final Object message, final long timeout) throws TimeoutException, InterruptedException, ExecutionException {
        return post(message, null, timeout);
    }

    public Object post(final Object message, Predicate<Object> responseFilter, final TimeSpan timeout) throws TimeoutException, InterruptedException, ExecutionException {
        responseFilter = Predicates.and(exceptIncoming(message), responseFilter);
        final IncomingMessageEvent event = new IncomingMessageEvent(responseFilter);
        final Future<?> awaitor = event.getAwaitor();
        register(event);
        post(message);
        try {
            return awaitor.get(timeout.duration, timeout.unit);
        }
        finally {
            unregister(event);
        }
    }

    /**
     * Posts message synchronously.
     * @param message Message to send. Cannot be {@literal null}.
     * @param responseFilter A filter object used to detect response. Cannot be {@literal null}.
     * @param timeout Response timeout, in millis.
     * @return Response message.
     * @throws TimeoutException Unable to receive response in the specified amount of time.
     * @throws InterruptedException The caller thread is interrupted.
     * @throws ExecutionException Filter raises exception.
     */
    public Object post(final Object message, final Predicate<Object> responseFilter, final long timeout) throws TimeoutException, InterruptedException, ExecutionException {
        return post(message, responseFilter, TimeSpan.ofMillis(timeout));
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
