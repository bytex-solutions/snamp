package com.itworks.snamp;

import com.google.common.eventbus.EventBus;

import java.util.EventListener;

/**
 * Represents helper methods for {@link com.google.common.eventbus.EventBus}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class EventBusManager {
    private EventBusManager(){

    }

    /**
     * Represents typed event emitter.
     * @param <M> Type of the event(or message) to emit.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface Publisher<M>{
        /**
         * Emits the message.
         * @param message The message to emit.
         */
        void post(final M message);
    }

    /**
     * Represents subscription manager.
     * @param <H> Type of the event bus listener.
     * @author Roman Sakno
     * @since 1.0
     */
    public static interface SubscriptionManager<H extends EventListener>{
        void subscribe(final H handler);
        void unsubscribe(final H handler);
    }

    /**
     * Extracts emitting interface from event bus.
     * @param bus The bus object to split.
     * @param <M> Type of the message.
     * @return An emitter that emits an events of the specified type.
     */
    public static <M> Publisher<M> getEmitter(final EventBus bus) {
        return new Publisher<M>() {
            @Override
            public void post(final M message) {
                bus.post(message);
            }
        };
    }

    /**
     * Extracts subscription management interface from event bus.
     * @param bus The bus object to split.
     * @param <H> Type of the event handlers.
     * @return Subscription manager.
     */
    public static <H extends EventListener> SubscriptionManager<H> getSubscriptionManager(final EventBus bus){
        return new SubscriptionManager<H>() {
            @Override
            public void subscribe(final H handler) {
                bus.register(handler);
            }

            @Override
            public void unsubscribe(final H handler) {
                bus.unregister(handler);
            }
        };
    }
}
