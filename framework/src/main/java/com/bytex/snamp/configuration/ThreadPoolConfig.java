package com.bytex.snamp.configuration;

import com.google.common.base.Supplier;
import com.bytex.snamp.concurrent.GroupedThreadFactory;

import java.util.Map;
import java.util.concurrent.*;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityConfiguration;
import static com.bytex.snamp.configuration.ThreadPoolConfigurationDescriptor.*;

/**
 * Represents configuration of the thread pool.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ThreadPoolConfig implements Supplier<ExecutorService> {
    /**
     * Represents infinite queue size.
     */
    public static final int INFINITE_QUEUE_SIZE = Integer.MAX_VALUE;
    protected final int minPoolSize;
    protected final int maxPoolSize;
    protected final long keepAliveTime;
    protected final int queueSize;
    protected final GroupedThreadFactory threadFactory;

    public ThreadPoolConfig(final String threadGroupName,
                            final int minPoolSize,
                            final int maxPoolSize,
                            final int queueSize,
                            final long keepAliveTimeMillis,
                            final int threadPriority) {
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTimeMillis;
        this.threadFactory = new GroupedThreadFactory(threadGroupName, threadPriority);
        this.queueSize = queueSize;
    }

    public ThreadPoolConfig(final Map<String, String> properties,
                            final String threadGroupName,
                            final int defaultMinPoolSize,
                            final int defaultMaxPoolSize,
                            final int defaultQueueSize,
                            final long defaultKeepAliveTimeMillis,
                            final int defaultThreadPriority) {
        this(threadGroupName,
                properties.containsKey(MIN_POOL_SIZE_PROPERTY) ?
                        Integer.parseInt(properties.get(MIN_POOL_SIZE_PROPERTY)) :
                        defaultMinPoolSize,
                properties.containsKey(MAX_POOL_SIZE_PROPERTY) ?
                        Integer.parseInt(properties.get(MAX_POOL_SIZE_PROPERTY)) :
                        defaultMaxPoolSize,
                properties.containsKey(QUEUE_SIZE_PROPERTY) ?
                        Integer.parseInt(properties.get(QUEUE_SIZE_PROPERTY)) :
                        defaultQueueSize,
                properties.containsKey(KEEP_ALIVE_TIME_PROPERTY) ?
                        Integer.parseInt(properties.get(KEEP_ALIVE_TIME_PROPERTY)) :
                        defaultKeepAliveTimeMillis,
                properties.containsKey(PRIORITY_PROPERTY) ?
                        Integer.parseInt(properties.get(PRIORITY_PROPERTY)) :
                        defaultThreadPriority);
    }

    public ThreadPoolConfig(final EntityConfiguration entity,
                            final String threadGroupName,
                            final int defaultMinPoolSize,
                            final int defaultMaxPoolSize,
                            final int defaultQueueSize,
                            final long defaultKeepAliveTimeMillis,
                            final int defaultThreadPriority) {
        this(entity.getParameters(), threadGroupName,
                defaultMinPoolSize,
                defaultMaxPoolSize,
                defaultQueueSize,
                defaultKeepAliveTimeMillis,
                defaultThreadPriority);
    }

    /**
     * Creates a new instance of the empty task queue.
     * @return A new instance of the empty task queue.
     */
    protected final BlockingQueue<Runnable> createTaskQueue(){
        if(queueSize == INFINITE_QUEUE_SIZE)
            return new LinkedBlockingQueue<>();
        else return new ArrayBlockingQueue<>(queueSize);
    }

    /**
     * Create a new thread pool.
     *
     * @return A new thread pool.
     */
    @Override
    public ExecutorService get() {
        return new ThreadPoolExecutor(minPoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                createTaskQueue(), threadFactory);
    }
}
