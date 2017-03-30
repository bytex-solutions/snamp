package com.bytex.snamp.configuration.impl;

import java.io.IOException;
import java.util.Dictionary;

import static com.bytex.snamp.concurrent.ThreadPoolRepository.DEFAULT_POOL;

/**
 * Represents parser of {@link SerializableThreadPoolConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class CMThreadPoolParser extends SerializableConfigurationParser<SerializableThreadPoolConfiguration> {
    public static final String PID = "com.bytex.snamp.concurrency.threadPools";

    CMThreadPoolParser() {
        super(SerializableAgentConfiguration::getThreadPools, PID, SerializableThreadPoolConfiguration.class, DEFAULT_POOL);
    }

    public static SerializableThreadPoolConfiguration deserialize(final String poolName,
                                                                  final Dictionary<String, ?> properties,
                                                                  final ClassLoader caller) throws IOException {
        return deserialize(poolName, SerializableThreadPoolConfiguration.class, properties, caller);
    }
}
