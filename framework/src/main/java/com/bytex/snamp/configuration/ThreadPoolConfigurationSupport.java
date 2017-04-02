package com.bytex.snamp.configuration;

import com.bytex.snamp.concurrent.ThreadPoolRepository;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents interface with configuration parameter specifies thread pool name.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface ThreadPoolConfigurationSupport extends Map<String, String> {
    /**
     * Represents name of configuration parameter that points to thread pool in {@link com.bytex.snamp.concurrent.ThreadPoolRepository}
     * service used by gateway.
     */
    String THREAD_POOL_KEY = "threadPool";

    default String getThreadPool() {
        return getOrDefault(THREAD_POOL_KEY, ThreadPoolRepository.DEFAULT_POOL);
    }

    default void setThreadPool(final String value){
        if(isNullOrEmpty(value))
            remove(THREAD_POOL_KEY);
        else
            put(THREAD_POOL_KEY, value);
    }
}
