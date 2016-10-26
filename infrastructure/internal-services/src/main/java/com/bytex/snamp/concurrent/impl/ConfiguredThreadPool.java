package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Represents thread pool with its initial configuration.
 */
final class ConfiguredThreadPool implements ExecutorService, ThreadPoolConfiguration {
    private final ThreadPoolConfiguration configuration;
    private final ExecutorService threadPool;

    ConfiguredThreadPool(final ThreadPoolConfiguration config, final String threadGroup) {
        final GroupedThreadFactory threadFactory = new GroupedThreadFactory(threadGroup, config.getThreadPriority());
        final BlockingQueue<Runnable> taskQueue;
        final int corePoolSize;
        if (config.getQueueSize() == INFINITE_QUEUE_SIZE) {
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
        threadPool = new ThreadPoolExecutor(corePoolSize,
                config.getMaxPoolSize(),
                config.getKeepAliveTime().toMillis(),
                TimeUnit.MILLISECONDS,
                taskQueue,
                threadFactory);
        configuration = config;
    }

    @Override
    public int getMinPoolSize() {
        return configuration.getMinPoolSize();
    }

    @Override
    public void setMinPoolSize(final int value) {
        configuration.setMinPoolSize(value);
    }

    @Override
    public int getMaxPoolSize() {
        return configuration.getMaxPoolSize();
    }

    @Override
    public void setMaxPoolSize(final int value) {
        configuration.setMaxPoolSize(value);
    }

    @Override
    public Duration getKeepAliveTime() {
        return configuration.getKeepAliveTime();
    }

    @Override
    public void setKeepAliveTime(Duration value) {

    }

    @Override
    public int getQueueSize() {
        return INFINITE_QUEUE_SIZE;
    }

    @Override
    public void setQueueSize(int value) {

    }

    @Override
    public int getThreadPriority() {
        return DEFAULT_THREAD_PRIORITY;
    }

    @Override
    public void setThreadPriority(int value) {

    }

    @Override
    public Map<String, String> getParameters() {
        return configuration.getParameters();
    }

    @Override
    public void shutdown() {
        threadPool.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return threadPool.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return threadPool.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPool.isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return threadPool.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return threadPool.submit(task);
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        return threadPool.submit(task, result);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return threadPool.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return threadPool.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
        return threadPool.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return threadPool.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return threadPool.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(final Runnable command) {
        threadPool.execute(command);
    }

    @Override
    public int hashCode() {
        return configuration.hashCode();
    }

    private boolean equals(final ThreadPoolConfiguration other){
        return configuration.equals(other);
    }

    private boolean equals(final ExecutorService other){
        return threadPool.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ThreadPoolConfiguration ?
                equals((ThreadPoolConfiguration) other) :
                other instanceof ExecutorService && equals((ExecutorService) other);
    }
}
