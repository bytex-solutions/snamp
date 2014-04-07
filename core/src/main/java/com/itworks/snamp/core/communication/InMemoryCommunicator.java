package com.itworks.snamp.core.communication;

import java.util.concurrent.*;

/**
 * Represents an abstract class that allows to build in-memory communicator.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class InMemoryCommunicator extends AbstractCommunicator implements AutoCloseable {
    private final ExecutorService executor;

    /**
     * Initializes a new in-memory communicator using the specified task scheduler used to
     * process message flows.
     * @param executor Task scheduler that is used to process message flows. Cannot be {@literal null}.
     * @throws IllegalArgumentException executor is {@literal null}.
     */
    protected InMemoryCommunicator(final ExecutorService executor){
        if(executor == null) throw new IllegalArgumentException("executor is null.");
        else this.executor = executor;
    }

    /**
     * Initializes a new in-memory communicator with the specified capacity of thread pool
     * using to process message flows.
     * @param nThreads Capacity of thread pool using to process message flows.
     */
    protected InMemoryCommunicator(final int nThreads){
        this(Executors.newFixedThreadPool(nThreads));
    }

    /**
     * Sends one-way message to the specified receiver.
     * @param sender   The message sender.
     * @param message  The message to be sent.
     * @param receiver The message receiver.
     * @param <REQ>    Type of the request.
     */
    @Override
    protected final <REQ> void sendSignal(final CommunicableObject sender, final InputMessage<REQ, Void> message, final CommunicableObject receiver) {
        executor.submit(new Runnable() {
            @Override
            public final void run() {
                try {
                    receiver.processMessage(sender, message);
                }
                catch (final Exception e) {
                    //just ignores the message
                }
            }
        });
    }

    /**
     * Sends message to the specified receiver.
     * @param sender       The message sender.
     * @param inputMessage The message to be sent.
     * @param receiver     The message receiver.
     * @param <RES>        Type of the response.
     * @param <REQ>        Type of the request.
     * @return An object that controls asynchronous state of the response.
     */
    @Override
    protected final <REQ, RES> Future<RES> sendMessage(final CommunicableObject sender, final InputMessage<REQ, RES> inputMessage, final CommunicableObject receiver) {
        return executor.submit(new Callable<RES>() {
            @Override
            public final RES call() throws Exception {
                return receiver.processMessage(sender, inputMessage);
            }
        });
    }

    /**
     * Releases all resources associated with this communicator.
     */
    @Override
    public void close() {
        executor.shutdown();
    }
}
