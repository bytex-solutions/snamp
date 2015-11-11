package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.configuration.ThreadPoolConfig;

import java.util.Map;

/**
 * Represents configuration of thread pool used by MQ Connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MQThreadPoolConfig extends ThreadPoolConfig {
    MQThreadPoolConfig(final Map<String, String> parameters, final String resourceName) throws NumberFormatException {
        super(parameters, "mq#" + resourceName, 10000);
    }
}
