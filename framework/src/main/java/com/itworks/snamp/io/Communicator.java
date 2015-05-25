package com.itworks.snamp.io;

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
        private final int exclusionHashCode;

        private IncomingMessageEvent(final Object exclusion) {
            super(false);
            this.exclusionHashCode = System.identityHashCode(exclusion);
        }

        @Subscribe
        @SpecialUse
        @AllowConcurrentEvents
        public void accept(final Object message) {
            if(exclusionHashCode != System.identityHashCode(message))
                fire(message);
        }
    }
    //communication sessions
    private static final Cache<String, Communicator> communicators =
            CacheBuilder.newBuilder().weakValues().build();

    private Communicator(){
    }

    public static Communicator getSession(final String name) throws ExecutionException {
        return communicators.get(name, new Callable<Communicator>() {
            @Override
            public Communicator call() {
                return new Communicator();
            }
        });
    }

    public Object post(final Object message, final TimeSpan timeout) throws TimeoutException, InterruptedException {
        final IncomingMessageEvent event = new IncomingMessageEvent(message);
        final IncomingMessageEvent.EventAwaitor<Object> awaitor = event.getAwaitor();
        register(event);
        post(message);
        try {
            return awaitor.await(timeout);
        } finally {
            unregister(event);
        }
    }

    public Object post(final Object message, final long timeout) throws TimeoutException, InterruptedException {
        return post(message, new TimeSpan(timeout));
    }

    public SynchronizationEvent<?> registerMessageSynchronizer(final Object except){
        final IncomingMessageEvent event = new IncomingMessageEvent(except);
        register(event);
        return event;
    }
}
