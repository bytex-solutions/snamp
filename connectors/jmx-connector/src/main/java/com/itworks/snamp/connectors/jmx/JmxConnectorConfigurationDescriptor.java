package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Represents JMX connector configuration descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    static final String JMX_LOGIN = "login";
    static final String JMX_PASSWORD = "password";
    static final String CONNECTION_RETRY_COUNT = "retryCount";
    static final String OBJECT_NAME_PROPERTY = "objectName";
    static final String SEVERITY_PARAM = "severity";
    static final String USE_REGEXP_PARAM = "useRegexp";

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "JmxEventConfig";

        private EventConfigurationInfo(){
            super(EventConfiguration.class,
                    SEVERITY_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc != null ? loc : Locale.getDefault());
        }
    }

    private static final class ConnectorConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration> {
        private static final String RESOURCE_NAME = "JmxConnectorConfig";

        private ConnectorConfigurationInfo() {
            super(ManagedResourceConfiguration.class,
                    JMX_LOGIN,
                    JMX_PASSWORD,
                    CONNECTION_RETRY_COUNT);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc != null ? loc : Locale.getDefault());
        }
    }

    /**
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration> {
        private static final String RESOURCE_NAME = "JmxAttributeConfig";

        private AttributeConfigurationInfo() {
            super(AttributeConfiguration.class,
                    OBJECT_NAME_PROPERTY,
                    USE_REGEXP_PARAM);
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
        super(new AttributeConfigurationInfo(), new ConnectorConfigurationInfo(), new EventConfigurationInfo());
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
