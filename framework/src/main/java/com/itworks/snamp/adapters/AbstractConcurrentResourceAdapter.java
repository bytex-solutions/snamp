package com.itworks.snamp.adapters;

import com.google.common.base.Supplier;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Represents resource adapter that uses concurrency.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractConcurrentResourceAdapter extends AbstractResourceAdapter {
    private final Supplier<ExecutorService> threadPoolFactory;
    private ExecutorService threadPool;

    /**
     * Initializes a new resource adapter.
     * @param instanceName The name of the adapter instance.
     * @param threadPoolFactory A thread pool factory that is used to create thread pool for managing incoming requests. Cannot be {@literal null}.
     */
    protected AbstractConcurrentResourceAdapter(final String instanceName,
                                                final Supplier<ExecutorService> threadPoolFactory) {
        super(instanceName);
        this.threadPoolFactory = Objects.requireNonNull(threadPoolFactory, "threadPoolFactory is null.");
        this.threadPool = null;
    }

    protected abstract void start(final ExecutorService threadPool) throws Exception;

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     * @throws java.lang.Exception Unable to start adapter.
     */
    @Override
    protected final void start() throws Exception{
        if((threadPool = threadPoolFactory.get()) == null)
            throw new IllegalStateException("Unable to create ThreadPool");
        start(threadPool);
    }

    /**
     * <p>
     *      The default implementation of this method just call {@link java.util.concurrent.ExecutorService#shutdownNow()}
     *      method.
     * @param threadPool The thread pool to be released.
     * @throws java.lang.Exception Unable to stop adapter.
     */
    protected abstract void stop(final ExecutorService threadPool) throws Exception;

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * @throws java.lang.Exception Unable to stop adapter.
     */
    @Override
    protected final void stop() throws Exception{
        try {
            stop(threadPool);
        }
        finally {
            threadPool.shutdownNow();
            threadPool = null;
        }
    }
}
