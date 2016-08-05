package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Represents default settings of thread pool.
 */
final class DefaultThreadPoolConfiguration implements ThreadPoolConfiguration {
    private static final LazyValue<? extends ThreadPoolConfiguration> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(DefaultThreadPoolConfiguration::new);

    private DefaultThreadPoolConfiguration(){

    }

    static ThreadPoolConfiguration getInstance(){
        return INSTANCE.get();
    }

    @Override
    public int getMinPoolSize() {
        return DEFAULT_MIN_POOL_SIZE;
    }

    @Override
    public void setMinPoolSize(int value) {

    }

    @Override
    public int getMaxPoolSize() {
        return DEFAULT_MAX_POOL_SIZE;
    }

    @Override
    public void setMaxPoolSize(int value) {

    }

    @Override
    public Duration getKeepAliveTime() {
        return DEFAULT_KEEP_ALIVE_TIME;
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
        return Collections.emptyMap();
    }
}
