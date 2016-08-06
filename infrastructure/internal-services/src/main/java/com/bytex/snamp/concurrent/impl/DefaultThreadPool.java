package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.concurrent.ThreadPoolConfig;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents a proxy for SNAMP default thread pool.
 * <p>
 *     This proxy disables behavior of {@link ExecutorService#shutdown()} and {@link ExecutorService#shutdownNow()} methods.
 */
final class DefaultThreadPool extends ThreadPoolExecutor {
    private static final GroupedThreadFactory THREAD_FACTORY = new GroupedThreadFactory("SnampDefaultThreadPool", ThreadPoolConfig.DEFAULT_PRIORITY);

    private DefaultThreadPool(final ThreadPoolConfig config){
        super(config.getMinPoolSize(),
                config.getMaxPoolSize(),
                config.getKeepAliveTime().toMillis(),
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                THREAD_FACTORY);
    }

    DefaultThreadPool(){
        this(getConfig());
    }

    static ThreadPoolConfig getConfig(){
        return new ThreadPoolConfig();
    }

    @Override
    @MethodStub
    public void shutdown() {
        //avoid consumers of default thread pool to terminate it (uncontrolled termination leads to SNAMP crash).
    }

    @Override
    public List<Runnable> shutdownNow() {
        //see shutdown()
        return Collections.emptyList();
    }

    void terminate(){
        super.shutdown();
    }
}
