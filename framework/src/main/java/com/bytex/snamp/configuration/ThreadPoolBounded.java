package com.bytex.snamp.configuration;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface ThreadPoolBounded extends Map<String, String> {
    String getThreadPool();
}
