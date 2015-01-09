package com.itworks.snamp.connectors.rshell;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;

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
            super(AttributeConfiguration.class, COMMAND_PROFILE_PATH_PARAM);
        }

        /**
         * Retrieves resource accessor for the specified locale.
         * @param loc The requested localization of the resource. May be {@literal null}.
         * @return The resource accessor.
         */
        @Override
        protected ResourceBundle getBundle(Locale loc) {
            if(loc == null) loc = Locale.getDefault();
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc);
        }
    }

    RShellConnectorConfigurationDescriptor() {
        super(new AttributeConfigurationInfo());
    }

}
