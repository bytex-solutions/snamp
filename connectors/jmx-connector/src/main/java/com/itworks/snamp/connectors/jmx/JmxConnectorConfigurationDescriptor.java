package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

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
    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> {
        private static final String RESOURCE_NAME = "JmxAttributeConfig";

        public AttributeConfigurationInfo(){
            super(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class, RESOURCE_NAME, OBJECT_NAME_PROPERTY);
        }

        /**
         * Retrieves resource accessor for the specified locale.
         *
         * @param loc The requested localization of the resource. May be {@literal null}.
         * @return The resource accessor.
         */
        @Override
        protected final ResourceBundle getBundle(Locale loc) {
            if(loc == null) loc = Locale.getDefault();
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc);
        }
    }

    public JmxConnectorConfigurationDescriptor(){
        super(new AttributeConfigurationInfo());
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return JmxConnectorHelpers.getLogger();
    }
}
