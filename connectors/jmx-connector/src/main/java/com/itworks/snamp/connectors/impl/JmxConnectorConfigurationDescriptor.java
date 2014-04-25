package com.itworks.snamp.connectors.impl;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Represents JMX connector configuration descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    public static final String OBJECT_NAME_PROPERTY = "objectName";

    /**
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration> {
        private static final String RESOURCE_NAME = "JmxAttributeConfig";

        public AttributeConfigurationInfo(){
            super(AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration.class, RESOURCE_NAME, OBJECT_NAME_PROPERTY);
            final Object o = getClass().getResourceAsStream("JmxAttributeConfig.properties");
        }

        /**
         * Retrieves resource accessor for the specified locale.
         *
         * @param loc The requested localization of the resource. May be {@literal null}.
         * @return The resource accessor.
         */
        @Override
        protected final ResourceBundle getBundle(final Locale loc) {
            return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    public JmxConnectorConfigurationDescriptor(){
        super(new AttributeConfigurationInfo());
    }
}
