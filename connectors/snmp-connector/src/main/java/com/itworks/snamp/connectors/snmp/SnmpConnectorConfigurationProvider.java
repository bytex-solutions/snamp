package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * Represents SNMP connector configuration descriptor.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpConnectorConfigurationProvider extends ConfigurationEntityDescriptionProviderImpl {
    //connector related parameters
    static final String COMMUNITY_PARAM = "community";
    static final String ENGINE_ID_PARAM = "engineID";
    static final String USER_NAME_PARAM = "userName";
    static final String AUTH_PROTOCOL_PARAM = "authenticationProtocol";
    static final String ENCRYPTION_PROTOCOL_PARAM = "encryptionProtocol";
    static final String PASSWORD_PARAM = "password";
    static final String ENCRYPTION_KEY_PARAM = "encryptionKey";
    static final String LOCAL_ADDRESS_PARAM = "localAddress";
    static final String SECURITY_CONTEXT_PARAM = "securityContext";
    static final String SOCKET_TIMEOUT = "socketTimeout";
    static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    //attribute related parameters
    static final String SNMP_CONVERSION_FORMAT = "snmpConversionFormat";
    //event related parameters
    static final String SEVERITY_PARAM = "severity";
    static final String MESSAGE_TEMPLATE = "messageTemplate";

    private static final class EventConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventOptions";

        public EventConfigurationDescriptor(){
            super(EventConfiguration.class, SEVERITY_PARAM, MESSAGE_TEMPLATE);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ?
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc):
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeOptions";

        public AttributeConfigurationDescriptor(){
            super(AttributeConfiguration.class, SNMP_CONVERSION_FORMAT);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ?
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc):
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorOptions";

        public ConnectorConfigurationDescriptor(){
            super(ManagedResourceConfiguration.class,
                    COMMUNITY_PARAM,
                    ENGINE_ID_PARAM,
                    USER_NAME_PARAM,
                    AUTH_PROTOCOL_PARAM,
                    ENCRYPTION_KEY_PARAM,
                    ENCRYPTION_PROTOCOL_PARAM,
                    PASSWORD_PARAM,
                    LOCAL_ADDRESS_PARAM,
                    SECURITY_CONTEXT_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ?
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc):
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    public SnmpConnectorConfigurationProvider(){
        super(new ConnectorConfigurationDescriptor(),
                new AttributeConfigurationDescriptor(),
                new EventConfigurationDescriptor());
    }
}
