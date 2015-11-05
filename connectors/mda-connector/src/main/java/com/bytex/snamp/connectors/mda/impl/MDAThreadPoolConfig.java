package com.bytex.snamp.connectors.mda.impl;

import com.bytex.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * Represents configuration of thread pool for Monitoring Data Acceptors.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MDAThreadPoolConfig extends ThreadPoolConfig {
    private static final int DEFAULT_KEEP_ALIVE = 10000;

    public MDAThreadPoolConfig(final String resourceName, final Map<String, String> parameters){
        super(parameters,
                resourceName,
                DEFAULT_KEEP_ALIVE);
    }
}
