package com.bytex.snamp.configuration;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static com.bytex.snamp.internal.Utils.interfaceStaticInitialize;

/**
 * Represents configuration of thread pool.
 * @since 2.0
 * @version 2.0
 */
public interface ThreadPoolConfiguration extends EntityConfiguration {
    /**
     * Default maximum number of threads to allow in the pool
     */
    int DEFAULT_MAX_POOL_SIZE = interfaceStaticInitialize(() -> {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        return availableProcessors + availableProcessors / 2;
    });

    /**
     * Default number of threads to keep in the pool.
     */
    int DEFAULT_MIN_POOL_SIZE = Math.min(2, Runtime.getRuntime().availableProcessors());

    /**
     * When the number of threads is greater than the minimum, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating
     */
    Duration DEFAULT_KEEP_ALIVE_TIME = Duration.ofSeconds(1);

    /**
     * Infinite size of the queue used to enqueue scheduled tasks.
     */
    int INFINITE_QUEUE_SIZE = Integer.MAX_VALUE;

    int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;


    int getMinPoolSize();

    void setMinPoolSize(final int value);

    int getMaxPoolSize();

    void setMaxPoolSize(final int value);

    Duration getKeepAliveTime();

    void setKeepAliveTime(final Duration value);

    int getQueueSize();

    void setQueueSize(final int value);

    int getThreadPriority();

    void setThreadPriority(final int value);
}
