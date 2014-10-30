package com.itworks.snamp.adapters;

import com.google.common.base.Supplier;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * Represents resource adapter that uses concurrency.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractConcurrentResourceAdapter extends AbstractResourceAdapter {
    private final Supplier<ExecutorService> threadPoolFactory;
    private volatile ExecutorService threadPool;

    /**
     * Initializes a new resource adapter.
     *
     * @param resources A collection of managed resources to be exposed in protocol-specific manner
     *                  to the outside world.
     */
    protected AbstractConcurrentResourceAdapter(final Supplier<ExecutorService> threadPoolFactory, final Map<String, ManagedResourceConfiguration> resources) {
        super(resources);
        this.threadPoolFactory = Objects.requireNonNull(threadPoolFactory, "threadPoolFactory is null.");
        this.threadPool = null;
    }

    protected abstract boolean start(final ExecutorService threadPool);

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected final boolean start() {
        threadPool = threadPoolFactory.get();
        return threadPool != null && start(threadPool);
    }

    /**
     * <p>
     *      The default implementation of this method just call {@link java.util.concurrent.ExecutorService#shutdownNow()}
     *      method.
     * @param threadPool The thread pool to be released.
     */
    protected void stop(final ExecutorService threadPool){
        threadPool.shutdownNow();
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     */
    @Override
    protected final void stop() {
        stop(threadPool);
    }
}
