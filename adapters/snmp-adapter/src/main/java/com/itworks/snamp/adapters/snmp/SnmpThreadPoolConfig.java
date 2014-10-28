package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * Represents configuration of thread pool.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpThreadPoolConfig extends ThreadPoolConfig {
    private static final int DEFAULT_MIN_POOL_SIZE = 1;
    private static final int DEFAULT_MAX_POOL_SIZE = 5;
    private static final long DEFAULT_KEEP_ALIVE_TIME = 2000;
    private static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY;

    private static String createThreadGroup(final String adapterInstanceName){
        return String.format("%s:%s", SnmpHelpers.ADAPTER_NAME, adapterInstanceName);
    }

    SnmpThreadPoolConfig(final Map<String, String> parameters, final String adapterInstanceName){
        super(parameters, createThreadGroup(adapterInstanceName),
                DEFAULT_MIN_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE,
                INFINITE_QUEUE_SIZE,
                DEFAULT_KEEP_ALIVE_TIME,
                DEFAULT_PRIORITY);
    }
}
