package com.bytex.snamp.connectors.snmp;

import com.bytex.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * Represents configuration of thread pool.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpThreadPoolConfig extends ThreadPoolConfig {
    private static final long DEFAULT_KEEP_ALIVE_TIME = 2000;

    private static String createThreadGroup(final String resourceName){
        return String.format("%s:%s", SnmpResourceConnector.getConnectorType(), resourceName);
    }

    SnmpThreadPoolConfig(final Map<String, String> parameters, final String resourceName){
        super(parameters, createThreadGroup(resourceName),
                DEFAULT_KEEP_ALIVE_TIME);
    }
}
