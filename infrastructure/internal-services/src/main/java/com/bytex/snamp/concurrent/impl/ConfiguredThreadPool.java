package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Represents thread pool with its initial configuration.
 */
final class ConfiguredThreadPool extends ThreadPoolExecutor implements ExecutorService {
    @Immutable
    private static final class ThreadPoolParameters{
        private final int corePoolSize;
        private final int maxPoolSize;
        private final long keepAliveTime;
        private final TimeUnit keepAliveTimeUnit;
        private final BlockingQueue<Runnable> taskQueue;
        private final ThreadFactory threadFactory;

        private ThreadPoolParameters(@Nonnull final ThreadPoolConfiguration config, @Nonnull final String threadGroup){
            threadFactory = new GroupedThreadFactory(threadGroup, config.getThreadPriority());
            if (config.getQueueSize() == ThreadPoolConfiguration.INFINITE_QUEUE_SIZE) {
                /*
                    Using an unbounded queue  will cause new tasks to wait in the queue when all corePoolSize
                    threads are busy. Thus, no more than corePoolSize threads will ever be created.
                 */
                if (config.getMaxPoolSize() == Integer.MAX_VALUE) {
                    taskQueue = new SynchronousQueue<>();
                    corePoolSize = config.getMinPoolSize();
                } else {
                    taskQueue = new LinkedBlockingQueue<>();
                    corePoolSize = config.getMaxPoolSize();
                }
            } else {
                taskQueue = config.getMaxPoolSize() == Integer.MAX_VALUE ?
                        new SynchronousQueue<>() :
                        new ArrayBlockingQueue<>(config.getQueueSize());
                corePoolSize = config.getMinPoolSize();
            }
            maxPoolSize = config.getMaxPoolSize();
            keepAliveTimeUnit = TimeUnit.MILLISECONDS;
            keepAliveTime = config.getKeepAliveTime().toMillis();
        }
    }

    private final ThreadPoolConfiguration configuration;

    private ConfiguredThreadPool(final ThreadPoolConfiguration config, final ThreadPoolParameters parameters){
        super(parameters.corePoolSize,
                parameters.maxPoolSize,
                parameters.keepAliveTime,
                parameters.keepAliveTimeUnit,
                parameters.taskQueue,
                parameters.threadFactory);
        this.configuration = Objects.requireNonNull(config);
    }

    ConfiguredThreadPool(@Nonnull final ThreadPoolConfiguration config, @Nonnull final String threadGroup) {
        this(config, new ThreadPoolParameters(config, threadGroup));
    }

    ConfiguredThreadPool() {
        this(createDefaultThreadPoolConfig(), "SnampThread");
    }

    boolean hasConfiguration(final ThreadPoolConfiguration configuration) {
        return Objects.equals(this.configuration, configuration);
    }

    private static @Nonnull ThreadPoolConfiguration createDefaultThreadPoolConfig() {
        @Immutable
        final class DefaultThreadPoolConfiguration extends HashMap<String, String> implements ThreadPoolConfiguration {
            private static final long serialVersionUID = -5001645853852294018L;

            @Override
            public int getMinPoolSize() {
                return DEFAULT_MIN_POOL_SIZE;
            }

            @Override
            public void setMinPoolSize(final int value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getMaxPoolSize() {
                return DEFAULT_MAX_POOL_SIZE;
            }

            @Override
            public void setMaxPoolSize(final int value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Duration getKeepAliveTime() {
                return DEFAULT_KEEP_ALIVE_TIME;
            }

            @Override
            public void setKeepAliveTime(final Duration value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getQueueSize() {
                return INFINITE_QUEUE_SIZE;
            }

            @Override
            public void setQueueSize(final int value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int getThreadPriority() {
                return DEFAULT_THREAD_PRIORITY;
            }

            @Override
            public void setThreadPriority(final int value) {
                throw new UnsupportedOperationException();
            }
        }

        return new DefaultThreadPoolConfiguration();
    }
}
