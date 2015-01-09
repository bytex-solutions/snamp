package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.itworks.snamp.configuration.ThreadPoolConfigurationDescriptor;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

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
    static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";
    static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    static final String RESPONSE_TIMEOUT_PARAM = "responseTimeout";
    private static final TimeSpan DEFAULT_RESPONSE_TIMEOUT = TimeSpan.fromSeconds(6);
    //attribute related parameters
    static final String SNMP_CONVERSION_FORMAT_PARAM = "snmpConversionFormat";
    //event related parameters
    static final String SEVERITY_PARAM = "severity";
    static final String MESSAGE_TEMPLATE_PARAM = "messageTemplate";
    static final String MESSAGE_OID_PARAM = "messageOID";

    private static final class EventConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventOptions";

        public EventConfigurationDescriptor(){
            super(EventConfiguration.class, SEVERITY_PARAM, MESSAGE_TEMPLATE_PARAM);
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
            super(AttributeConfiguration.class, SNMP_CONVERSION_FORMAT_PARAM, RESPONSE_TIMEOUT_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return loc != null ?
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc):
                    ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
        }
    }

    private static final class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration> implements ThreadPoolConfigurationDescriptor<ManagedResourceConfiguration>{
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
                    SECURITY_CONTEXT_PARAM,
                    QUEUE_SIZE_PROPERTY,
                    PRIORITY_PROPERTY,
                    KEEP_ALIVE_TIME_PROPERTY,
                    MIN_POOL_SIZE_PROPERTY,
                    MAX_POOL_SIZE_PROPERTY);
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

    static TimeSpan getResponseTimeout(final Map<String, String> attributeParams){
        return attributeParams.containsKey(RESPONSE_TIMEOUT_PARAM) ?
                new TimeSpan(Integer.parseInt(attributeParams.get(RESPONSE_TIMEOUT_PARAM))):
                DEFAULT_RESPONSE_TIMEOUT;
    }
}
