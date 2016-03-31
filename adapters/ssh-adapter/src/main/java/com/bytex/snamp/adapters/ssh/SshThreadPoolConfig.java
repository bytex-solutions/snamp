package com.bytex.snamp.adapters.ssh;

import com.bytex.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class SshThreadPoolConfig extends ThreadPoolConfig {
    private static final long DEFAULT_KEEP_ALIVE_TIME = 2000;

    private static String createThreadGroup(final String adapterInstanceName){
        return String.format("%s:%s", SshHelpers.ADAPTER_NAME, adapterInstanceName);
    }

    SshThreadPoolConfig(final String instanceName,
                        final Map<String, String> parameters){
        super(parameters, createThreadGroup(instanceName),
                DEFAULT_KEEP_ALIVE_TIME);
    }
}
