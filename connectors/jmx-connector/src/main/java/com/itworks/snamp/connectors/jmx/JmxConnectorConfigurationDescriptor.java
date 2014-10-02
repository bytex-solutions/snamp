package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Represents JMX connector configuration descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    public static final String JMX_LOGIN = "login";
    public static final String JMX_PASSWORD = "password";
    public static final String CONNECTION_RETRY_COUNT = "retryCount";
    public static final String OBJECT_NAME_PROPERTY = "objectName";
    public static final String SEVERITY_PARAM = "severity";

    /**
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration> {
        private static final String RESOURCE_NAME = "JmxAttributeConfig";

        private AttributeConfigurationInfo(){
            super(AttributeConfiguration.class, OBJECT_NAME_PROPERTY);
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

    JmxConnectorConfigurationDescriptor(){
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
