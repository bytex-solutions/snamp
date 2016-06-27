package com.bytex.snamp.connectors.snmp;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.LazyContainers;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.connectors.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * Represents SNMP connector configuration descriptor.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class SnmpConnectorDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    //connector related parameters
    private static final String COMMUNITY_PARAM = "community";
    private static final String ENGINE_ID_PARAM = "engineID";
    private static final String USER_NAME_PARAM = "userName";
    private static final String AUTH_PROTOCOL_PARAM = "authenticationProtocol";
    private static final String ENCRYPTION_PROTOCOL_PARAM = "encryptionProtocol";
    private static final String PASSWORD_PARAM = "password";
    private static final String ENCRYPTION_KEY_PARAM = "encryptionKey";
    private static final String LOCAL_ADDRESS_PARAM = "localAddress";
    private static final String SECURITY_CONTEXT_PARAM = "securityContext";
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";
    private static final int DEFAULT_SOCKET_TIMEOUT = 3000;
    private static final String RESPONSE_TIMEOUT_PARAM = "responseTimeout";
    private static final TimeSpan DEFAULT_RESPONSE_TIMEOUT = TimeSpan.ofSeconds(6);
    //attribute related parameters
    static final String SNMP_CONVERSION_FORMAT_PARAM = "snmpConversionFormat";
    //event related parameters
    private static final String SEVERITY_PARAM = "severity";
    static final String MESSAGE_TEMPLATE_PARAM = "messageTemplate";
    static final String MESSAGE_OID_PARAM = "messageOID";

    private static final class EventConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventOptions";

        private EventConfigurationDescriptor(){
            super(RESOURCE_NAME, EventConfiguration.class, SEVERITY_PARAM, MESSAGE_TEMPLATE_PARAM);
        }
    }

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeOptions";

        private AttributeConfigurationDescriptor(){
            super(RESOURCE_NAME,
                    AttributeConfiguration.class,
                    SNMP_CONVERSION_FORMAT_PARAM,
                    RESPONSE_TIMEOUT_PARAM);
        }
    }

    private static final class ConnectorConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorOptions";

        private ConnectorConfigurationDescriptor(){
            super(RESOURCE_NAME,
                    ManagedResourceConfiguration.class,
                    COMMUNITY_PARAM,
                    ENGINE_ID_PARAM,
                    USER_NAME_PARAM,
                    AUTH_PROTOCOL_PARAM,
                    ENCRYPTION_KEY_PARAM,
                    ENCRYPTION_PROTOCOL_PARAM,
                    PASSWORD_PARAM,
                    LOCAL_ADDRESS_PARAM,
                    SECURITY_CONTEXT_PARAM,
                    THREAD_POOL_KEY,
                    SMART_MODE_KEY);
        }
    }

    private static final LazyValue<SnmpConnectorDescriptionProvider> INSTANCE = LazyContainers.NORMAL.create(SnmpConnectorDescriptionProvider::new);

    private SnmpConnectorDescriptionProvider(){
        super(new ConnectorConfigurationDescriptor(),
                new AttributeConfigurationDescriptor(),
                new EventConfigurationDescriptor());
    }

    static SnmpConnectorDescriptionProvider getInstance(){
        return INSTANCE.get();
    }

    static TimeSpan getResponseTimeout(final AttributeDescriptor attributeParams){
        return attributeParams.hasField(RESPONSE_TIMEOUT_PARAM) ?
                TimeSpan.ofMillis(attributeParams.getField(RESPONSE_TIMEOUT_PARAM, String.class)):
                DEFAULT_RESPONSE_TIMEOUT;
    }

    private OctetString parseEngineID(final Map<String, String> parameters) {
        if (parameters.containsKey(ENGINE_ID_PARAM))
            return OctetString.fromHexString(parameters.get(ENGINE_ID_PARAM));
        else return new OctetString(MPv3.createLocalEngineID());
    }

    private static OctetString parseCommunity(final Map<String, String> parameters){
        if(parameters.containsKey(COMMUNITY_PARAM))
            return new OctetString(parameters.get(COMMUNITY_PARAM));
        else return new OctetString("public");
    }

    private static OID getAuthenticationProtocol(final String authProtocol){
        if(authProtocol == null || authProtocol.isEmpty()) return null;
        else switch (authProtocol.toLowerCase()){
            case "md-5":
            case "md5": return AuthMD5.ID;
            case "sha": return AuthSHA.ID;
            default: return new OID(authProtocol);
        }
    }

    private static OID getEncryptionProtocol(final String encryptionProtocol){
        if(encryptionProtocol == null || encryptionProtocol.isEmpty()) return null;
        else switch (encryptionProtocol.toLowerCase()){
            case "aes-128":
            case "aes128":return PrivAES128.ID;
            case "aes-192":
            case "aes192": return PrivAES192.ID;
            case "aes-256":
            case "aes256": return PrivAES256.ID;
            case "des": return PrivDES.ID;
            case "3des":
            case "3-des": return Priv3DES.ID;
            default: return new OID(encryptionProtocol);
        }
    }

    SnmpClient createSnmpClient(final Address connectionAddress, final Map<String, String> parameters) throws IOException{
        final OctetString engineID = parseEngineID(parameters);
        final OctetString community = parseCommunity(parameters);
        final ExecutorService threadPool = getThreadPool(parameters);
        final OctetString userName = parameters.containsKey(USER_NAME_PARAM) ?
                new OctetString(parameters.get(USER_NAME_PARAM)) :
                null;
        final OID authProtocol = getAuthenticationProtocol(parameters.containsKey(AUTH_PROTOCOL_PARAM) ?
                parameters.get(AUTH_PROTOCOL_PARAM) :
                null);
        final OctetString password = parameters.containsKey(PASSWORD_PARAM) ?
                new OctetString(parameters.get(PASSWORD_PARAM)):
                null;
        final OID encryptionProtocol = getEncryptionProtocol(parameters.containsKey(ENCRYPTION_PROTOCOL_PARAM)  ?
                parameters.get(ENCRYPTION_PROTOCOL_PARAM) :
                null);
        final OctetString encryptionKey = parameters.containsKey(ENCRYPTION_KEY_PARAM) ?
                new OctetString(parameters.get(ENCRYPTION_KEY_PARAM)) :
                null;
        final Address localAddress = parameters.containsKey(LOCAL_ADDRESS_PARAM) ?
                GenericAddress.parse(parameters.get(LOCAL_ADDRESS_PARAM)) :
                null;
        final OctetString securityContext = parameters.containsKey(SECURITY_CONTEXT_PARAM) ?
                new OctetString(parameters.get(SECURITY_CONTEXT_PARAM)) :
                null;
        final int socketTimeout = parameters.containsKey(SOCKET_TIMEOUT_PARAM) ?
                Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM)) :
                DEFAULT_SOCKET_TIMEOUT;

        return userName == null ?
                SnmpClient.create(connectionAddress, community, localAddress, socketTimeout, threadPool):
                SnmpClient.create(connectionAddress, engineID, userName, authProtocol, password, encryptionProtocol, encryptionKey, securityContext, localAddress, socketTimeout, threadPool);
    }
}
