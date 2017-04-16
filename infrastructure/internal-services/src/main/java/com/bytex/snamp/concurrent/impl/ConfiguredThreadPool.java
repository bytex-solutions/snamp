package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
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
        this(new DefaultThreadPoolConfiguration(), "SnampThread");
    }

    boolean hasConfiguration(final ThreadPoolConfiguration configuration) {
        return Objects.equals(this.configuration, configuration);
    }
}
