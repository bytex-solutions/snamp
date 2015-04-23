package com.itworks.snamp.connectors.wmq;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;

import java.net.URI;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MQConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    /**
     * The name of the MQ manager.
     */
    private static final String QUEUE_MANAGER_PARAM = "queueManager";

    static int getPort(final URI connectionString){
        return connectionString.getPort() > 0 ? connectionString.getPort() : 1414;
    }

    static String getQueueManagerName(final Map<String, String> parameters){
        if(parameters.containsKey(QUEUE_MANAGER_PARAM))
            return parameters.get(QUEUE_MANAGER_PARAM);
        else return "";
    }
}
