package com.itworks.snamp.connectors.groovy.impl;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GroovyResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    private static final String INIT_SCRIPT = "initScript";

    static String getInitScriptFile(final Map<String, String> parameters){
        return parameters.get(INIT_SCRIPT);
    }

    private static final class ConnectorConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorParameters";

        private ConnectorConfigurationInfo(){
            super(RESOURCE_NAME, ManagedResourceConfiguration.class, INIT_SCRIPT);
        }
    }

    GroovyResourceConfigurationDescriptor(){
        super(new ConnectorConfigurationInfo());
    }
}
