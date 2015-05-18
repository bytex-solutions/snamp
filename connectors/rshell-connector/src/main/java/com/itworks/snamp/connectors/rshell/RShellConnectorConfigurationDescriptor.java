package com.itworks.snamp.connectors.rshell;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Represents configuration descriptor for the RShell connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RShellConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    static final String COMMAND_PROFILE_PATH_PARAM = "commandProfileLocation";

    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "RShellAttributeConfig";

        private AttributeConfigurationInfo(){
            super(RESOURCE_NAME, AttributeConfiguration.class, COMMAND_PROFILE_PATH_PARAM);
        }
    }

    RShellConnectorConfigurationDescriptor() {
        super(new AttributeConfigurationInfo());
    }

}
