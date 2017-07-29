package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.concurrent.LazyReference;

import static com.bytex.snamp.concurrent.ThreadPoolRepository.DEFAULT_POOL;

/**
 * Represents parser of {@link SerializableThreadPoolConfiguration}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class DefaultThreadPoolParser extends SerializableConfigurationParser<SerializableThreadPoolConfiguration> {
    public static final String PID = "com.bytex.snamp.concurrency.threadPools";

    private static final LazyReference<DefaultThreadPoolParser> INSTANCE = LazyReference.soft();

    private DefaultThreadPoolParser() {
        super(SerializableAgentConfiguration::getThreadPools, PID, SerializableThreadPoolConfiguration.class, DEFAULT_POOL);
    }

    public static DefaultThreadPoolParser getInstance(){
        return INSTANCE.lazyGet(DefaultThreadPoolParser::new);
    }
}
