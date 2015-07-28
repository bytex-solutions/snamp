package com.bytex.snamp.adapters.nrdp;

import com.bytex.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SenderThreadPoolConfig extends ThreadPoolConfig {
    private static final int DEFAULT_MIN_POOL_SIZE = 1;
    private static final int DEFAULT_MAX_POOL_SIZE = 5;
    private static final long DEFAULT_KEEP_ALIVE_TIME = 2000;
    private static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;

    private static String createThreadGroup(final String adapterName, final String adapterInstanceName){
        return String.format("%s:%s", adapterName, adapterInstanceName);
    }

    SenderThreadPoolConfig(final Map<String, String> parameters,
                           final String adapterName,
                           final String adapterInstanceName){
        super(parameters, createThreadGroup(adapterName, adapterInstanceName),
                DEFAULT_MIN_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE,
                INFINITE_QUEUE_SIZE,
                DEFAULT_KEEP_ALIVE_TIME,
                DEFAULT_PRIORITY);
    }
}
