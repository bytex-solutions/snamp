package com.bytex.snamp.connectors.wmq;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.SMART_MODE_KEY;

import java.net.URI;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class MQConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    /**
     * The name of the MQ manager.
     */
    private static final String QUEUE_MANAGER_PARAM = "queueManager";

    private static final class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorParameters";

        private ConnectorConfigurationDescriptor(){
            super(RESOURCE_NAME, ManagedResourceConfiguration.class, QUEUE_MANAGER_PARAM, SMART_MODE_KEY);
        }
    }

    static int getPort(final URI connectionString){
        return connectionString.getPort() > 0 ? connectionString.getPort() : 1414;
    }

    static String getQueueManagerName(final Map<String, String> parameters){
        if(parameters.containsKey(QUEUE_MANAGER_PARAM))
            return parameters.get(QUEUE_MANAGER_PARAM);
        else return "";
    }
}
