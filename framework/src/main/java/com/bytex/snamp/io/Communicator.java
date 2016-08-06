package com.bytex.snamp.io;

import com.bytex.snamp.SpecialUse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.time.Duration;
import java.util.EventListener;
import java.util.concurrent.*;
import java.util.function.Predicate;

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
    private static final class IncomingMessageEvent extends CompletableFuture<Object> implements EventListener {
        private final Predicate<Object> responseFilter;

        private IncomingMessageEvent(final Predicate<Object> filter) {
            this.responseFilter = filter != null ? filter : obj -> true;
        }

        @Subscribe
        @SpecialUse
        @AllowConcurrentEvents
        public void accept(final Object message) {
            boolean success;
            try{
                success = responseFilter.test(message);
            }
            catch (final Throwable e){
                completeExceptionally(e);
                success = false;
            }
            if(success)
                complete(message);
        }
    }

    //communication sessions
    private static final Cache<String, Communicator> communicators =
            CacheBuilder.newBuilder().weakValues().build();

    private Communicator() {
    }

    public static Communicator getSession(final String name) throws ExecutionException {
        return communicators.get(name, Communicator::new);
    }

    private static Predicate<Object> exceptIncoming(final Object incomingMessage) {
        final int incomingIdentity = System.identityHashCode(incomingMessage);
        return actualMessage -> incomingIdentity != System.identityHashCode(actualMessage);
    }

    public Object post(final Object message, final Duration timeout) throws TimeoutException, InterruptedException, ExecutionException {
        return post(message, null, timeout);
    }

    public Object post(final Object message, final long timeout) throws TimeoutException, InterruptedException, ExecutionException {
        return post(message, null, timeout);
    }

    public Object post(final Object message, Predicate<Object> responseFilter, final Duration timeout) throws TimeoutException, InterruptedException, ExecutionException {
        responseFilter = exceptIncoming(message).and(responseFilter);
        final IncomingMessageEvent event = new IncomingMessageEvent(responseFilter);
        register(event);
        post(message);
        try {
            return event.get(timeout.toNanos(), TimeUnit.NANOSECONDS);
        } finally {
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
        return post(message, responseFilter, Duration.ofMillis(timeout));
    }

    public Future<?> registerMessageSynchronizer(final Object except) {
        return registerMessageSynchronizer(exceptIncoming(except));
    }

    public Future<?> registerMessageSynchronizer(final Predicate<Object> responseFilter) {
        final IncomingMessageEvent event = new IncomingMessageEvent(responseFilter);
        register(event);
        return event.whenComplete((r, e) -> unregister(event));
    }
}
