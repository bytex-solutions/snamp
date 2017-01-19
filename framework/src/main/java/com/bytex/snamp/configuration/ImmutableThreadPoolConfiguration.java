package com.bytex.snamp.configuration;

import java.time.Duration;

/**
 * Represents read-only copy of the thread pool configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ImmutableThreadPoolConfiguration extends ImmutableEntityConfiguration implements ThreadPoolConfiguration {
    private static final long serialVersionUID = -2855604941596388483L;
    private final int minPoolSize;
    private final int maxPoolSize;
    private final Duration keepAliveTime;
    private final int queueSize;
    private final int priority;

    ImmutableThreadPoolConfiguration(final ThreadPoolConfiguration entity) {
        super(entity);
        minPoolSize = entity.getMinPoolSize();
        maxPoolSize = entity.getMaxPoolSize();
        keepAliveTime = entity.getKeepAliveTime();
        queueSize = entity.getQueueSize();
        priority = entity.getThreadPriority();
    }

    ImmutableThreadPoolConfiguration(){
        minPoolSize = DEFAULT_MIN_POOL_SIZE;
        maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
        queueSize = INFINITE_QUEUE_SIZE;
        priority = DEFAULT_THREAD_PRIORITY;
    }

    @Override
    public ImmutableThreadPoolConfiguration asReadOnly() {
        return this;
    }

    @Override
    public int getMinPoolSize() {
        return minPoolSize;
    }

    @Override
    public void setMinPoolSize(final int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    @Override
    public void setMaxPoolSize(final int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Duration getKeepAliveTime() {
        return keepAliveTime;
    }

    @Override
    public void setKeepAliveTime(final Duration value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getQueueSize() {
        return queueSize;
    }

    @Override
    public void setQueueSize(final int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getThreadPriority() {
        return priority;
    }

    @Override
    public void setThreadPriority(final int value) {
        throw new UnsupportedOperationException();
    }
}
