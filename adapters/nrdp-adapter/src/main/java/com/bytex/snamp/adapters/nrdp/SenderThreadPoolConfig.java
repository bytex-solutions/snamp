package com.bytex.snamp.adapters.nrdp;

import com.bytex.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SenderThreadPoolConfig extends ThreadPoolConfig {

    private static final long DEFAULT_KEEP_ALIVE_TIME = 2000;

    private static String createThreadGroup(final String adapterName, final String adapterInstanceName){
        return String.format("%s:%s", adapterName, adapterInstanceName);
    }

    SenderThreadPoolConfig(final Map<String, String> parameters,
                           final String adapterName,
                           final String adapterInstanceName){
        super(parameters, createThreadGroup(adapterName, adapterInstanceName),
                DEFAULT_KEEP_ALIVE_TIME);
    }
}
