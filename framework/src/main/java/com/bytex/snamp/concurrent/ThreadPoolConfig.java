package com.bytex.snamp.concurrent;

import com.google.common.base.Function;
import com.google.common.primitives.Ints;

import java.io.Serializable;
import java.util.concurrent.*;

/**
 * Represents configuration of the thread pool.
 * @author Roman Sakno
 * @since 1.2
 * @version 1.2
 */
public final class ThreadPoolConfig implements Serializable, Function<String, ExecutorService> {
    public static final int DEFAULT_MIN_POOL_SIZE = 1;
    public static final int DEFAULT_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    public static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;
    public static final int DEFAULT_KEEP_ALIVE_TIME = 1000;
    public static final int INFINITE_QUEUE_SIZE = Integer.MAX_VALUE;

    private int minPoolSize = DEFAULT_MIN_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    private int threadPriority = DEFAULT_PRIORITY;
    private int keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
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

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(final int value) {
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

    public void useInifiniteQueue(){
        queueSize = INFINITE_QUEUE_SIZE;
    }

    @Override
    public ExecutorService apply(final String serviceName) {
        return createExecutorService(serviceName);
    }

    public ExecutorService createExecutorService(final String serviceName){
        final GroupedThreadFactory threadFactory = new GroupedThreadFactory(serviceName, threadPriority);
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
        return queueSize ^ (minPoolSize << 1) ^ (maxPoolSize << 2) ^ (threadPriority << 3) ^ (keepAliveTime << 4);
    }
}
