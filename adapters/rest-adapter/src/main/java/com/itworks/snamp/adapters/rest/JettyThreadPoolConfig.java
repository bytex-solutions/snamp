package com.itworks.snamp.adapters.rest;

import com.itworks.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JettyThreadPoolConfig extends ThreadPoolConfig {
    private static final int DEFAULT_MIN_POOL_SIZE = 5;
    private static final int DEFAULT_MAX_POOL_SIZE = 10;
    private static final long DEFAULT_KEEP_ALIVE_TIME = 60 * 1000;
    private static final int DEFAULT_QUEUE_SIZE = INFINITE_QUEUE_SIZE;
    private static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;

    private static String makeThreadGroupName(final String adapterInstance){
        return String.format("%s:%s", RestAdapterHelpers.ADAPTER_NAME, adapterInstance);
    }

    JettyThreadPoolConfig(final Map<String, String> parameters, final String adapterInstance) {
        super(parameters,
                makeThreadGroupName(adapterInstance),
                DEFAULT_MIN_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE,
                DEFAULT_QUEUE_SIZE,
                DEFAULT_KEEP_ALIVE_TIME,
                DEFAULT_PRIORITY);
    }
}
