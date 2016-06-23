package com.bytex.snamp.concurrent;

import com.bytex.snamp.TimeSpan;
import com.google.common.base.Function;

import java.io.Serializable;
import java.util.concurrent.*;

/**
 * Represents configuration of the thread pool.
 * @author Roman Sakno
 * @since 1.2
 * @version 1.2
 */
public final class ThreadPoolConfig implements Serializable, Function<String, ExecutorService> {
    private static final long serialVersionUID = -3096911128997326858L;
    /**
     * Default number of threads to keep in the pool.
     */
    public static final int DEFAULT_MIN_POOL_SIZE = 1;

    /**
     * Default maximum number of threads to allow in the pool
     */
    public static final int DEFAULT_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;

    /**
     * Default priority of threads in the pool.
     */
    public static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;

    /**
     * When the number of threads is greater than the minimum, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating
     */
    public static final TimeSpan DEFAULT_KEEP_ALIVE_TIME = TimeSpan.ofMillis(1000);

    /**
     * Infinite size of the queue used to enqueue scheduled tasks.
     */
    public static final int INFINITE_QUEUE_SIZE = Integer.MAX_VALUE;


    private int minPoolSize = DEFAULT_MIN_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    private int threadPriority = DEFAULT_PRIORITY;
    private TimeSpan keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    private int queueSize = INFINITE_QUEUE_SIZE;

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(final int value) {
        minPoolSize = value;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(final int value) {
        this.maxPoolSize = value;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public void setThreadPriority(final int value) {
        threadPriority = value;
    }

    public TimeSpan getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(final long millis) {
        keepAliveTime = TimeSpan.ofMillis(millis);
    }

    public void setKeepAliveTime(final TimeSpan value){
        keepAliveTime = value;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(final int value) {
        queueSize = value;
    }

    public boolean isInfiniteQueue(){
        return queueSize == INFINITE_QUEUE_SIZE;
    }

    /**
     * Setup infinite queue size used to store scheduled tasks.
     */
    public void useInfiniteQueue(){
        queueSize = INFINITE_QUEUE_SIZE;
    }

    /**
     * Creates a new thread pool using settings defined in this instance.
     * @param threadGroup Name of thread group.
     * @return A new instance of thread pool.
     */
    @Override
    public ExecutorService apply(final String threadGroup) {
        return createExecutorService(threadGroup);
    }

    /**
     * Creates a new thread pool using settings defined in this instance.
     * @param threadGroup Name of thread group.
     * @return A new instance of thread pool.
     */
    public ExecutorService createExecutorService(final String threadGroup){
        final GroupedThreadFactory threadFactory = new GroupedThreadFactory(threadGroup, threadPriority);
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
                taskQueue = maxPoolSize == Integer.MAX_VALUE ?
                        new SynchronousQueue<Runnable>() :
                        new ArrayBlockingQueue<Runnable>(queueSize);
                corePoolSize = minPoolSize;
                break;
        }
        return new ThreadPoolExecutor(corePoolSize,
                maxPoolSize,
                keepAliveTime.toMillis(),
                TimeUnit.MILLISECONDS,
                taskQueue,
                threadFactory);
    }

    public boolean equals(final ThreadPoolConfig other){
        return other != null &&
                queueSize == other.getQueueSize() &&
                minPoolSize == other.getMinPoolSize() &&
                maxPoolSize == other.getMaxPoolSize() &&
                threadPriority == other.getThreadPriority() &&
                keepAliveTime == other.getKeepAliveTime();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ThreadPoolConfig && equals((ThreadPoolConfig)other);
    }

    @Override
    public int hashCode() {
        return queueSize ^ (minPoolSize << 1) ^ (maxPoolSize << 2) ^ (threadPriority << 3) ^ (keepAliveTime.hashCode() << 4);
    }

    /**
     * Provides string representation of the thread pool configuration.
     * @return String representation of the thread pool configuration.
     */
    @Override
    public String toString() {
        return "{" +
                "minPoolSize=" + minPoolSize + ',' +
                "maxPoolSize=" + maxPoolSize + ',' +
                "threadPriority=" + threadPriority + ',' +
                "queueSize=" + (queueSize == INFINITE_QUEUE_SIZE ? "INFINITE" : queueSize) + ',' +
                "keepAliveTime" + keepAliveTime +
                '}';
    }
}
