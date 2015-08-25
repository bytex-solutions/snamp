package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * Represents configuration of thread pool for Monitoring Data Acceptors.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MdaThreadPoolConfig extends ThreadPoolConfig {
    private static final int DEFAULT_MIN_POOL_SIZE = 1;
    private static final int DEFAULT_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_KEEP_ALIVE = 10000;

    public MdaThreadPoolConfig(final String resourceName, final Map<String, String> parameters){
        super(parameters,
                resourceName,
                DEFAULT_MIN_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE,
                INFINITE_QUEUE_SIZE,
                DEFAULT_KEEP_ALIVE,
                Thread.NORM_PRIORITY);
    }
}
