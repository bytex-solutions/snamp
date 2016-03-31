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
 * @version 1.2.0
 * @since 1.0
 * @deprecated Use {@link com.bytex.snamp.concurrent.ThreadPoolConfig} instead. This class will be removed in the next version.
 */
@Deprecated
public class ThreadPoolConfig implements Supplier<ExecutorService> {
    /**
     * Represents infinite queue size.
     */
    public static final int INFINITE_QUEUE_SIZE = Integer.MAX_VALUE;
    /**
     * Recommended minimum count of threads in the pool.
     */
    public static final int RECOMMENDED_MIN_POOL_SIZE = 1;

    /**
     * Recommended maximum count of threads in the pool.
     */
    public static final int RECOMMENDED_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Recommended priority of threads in the pool.
     */
    public static final int RECOMMENDED_PRIORITY = Thread.NORM_PRIORITY;

    /**
     * Minimum count of threads in the pool.
     */
    protected final int minPoolSize;
    /**
     * Maximum count of threads in the pool.
     */
    protected final int maxPoolSize;
    protected final long keepAliveTime;
    protected final int queueSize;
    /**
     * Thread factory used to create new threads in the pool.
     */
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
                            final int defaultThreadPriority) throws NumberFormatException{
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
                            final int defaultThreadPriority) throws NumberFormatException{
        this(entity.getParameters(), threadGroupName,
                defaultMinPoolSize,
                defaultMaxPoolSize,
                defaultQueueSize,
                defaultKeepAliveTimeMillis,
                defaultThreadPriority);
    }

    public ThreadPoolConfig(final Map<String, String> parameters,
                            final String threadGroupName,
                            final long defaultKeepAliveTimeMillis) throws NumberFormatException{
        this(parameters,
                threadGroupName,
                RECOMMENDED_MIN_POOL_SIZE,
                RECOMMENDED_MAX_POOL_SIZE,
                INFINITE_QUEUE_SIZE,
                defaultKeepAliveTimeMillis,
                RECOMMENDED_PRIORITY);
    }

    /**
     * Create a new thread pool.
     *
     * @return A new thread pool.
     */
    @Override
    public ExecutorService get() {
        final BlockingQueue<Runnable> taskQueue;
        final int corePoolSize;
        switch (queueSize) {
            case INFINITE_QUEUE_SIZE:
                /*
                    Using an unbounded queue  will cause new tasks to wait in the queue when all corePoolSize
                    threads are busy. Thus, no more than corePoolSize threads will ever be created.
                 */
                if(maxPoolSize == Integer.MAX_VALUE){
                    taskQueue = new SynchronousQueue<>();
                    corePoolSize = minPoolSize;
                }
                else {
                    taskQueue = new LinkedBlockingQueue<>();
                    corePoolSize = maxPoolSize;
                }
                break;
            default:
                if(maxPoolSize == Integer.MAX_VALUE){
                    taskQueue = new SynchronousQueue<>();
                    corePoolSize = minPoolSize;
                }
                else {
                    taskQueue = new ArrayBlockingQueue<>(queueSize);
                    corePoolSize = minPoolSize;
                }
                break;
        }
        return new ThreadPoolExecutor(corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                taskQueue,
                threadFactory);
    }
}
