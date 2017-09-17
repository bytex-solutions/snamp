package com.bytex.snamp.connector.snmp;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.MapUtils.*;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.SMART_MODE_KEY;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.jmx.DescriptorUtils.parseStringField;

/**
 * Represents SNMP connector configuration descriptor.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
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
    private static final long DEFAULT_RESPONSE_TIMEOUT = 6000;
    private static final String DISCOVERY_TIMEOUT_PROPERTY = "discoveryTimeout";
    private static final long DEFAULT_DISCOVERY_TIMEOUT = 5000;
    //attribute related parameters
    static final String SNMP_CONVERSION_FORMAT_PARAM = "snmpConversionFormat";
    //event related parameters
    private static final String SEVERITY_PARAM = "severity";
    static final String MESSAGE_OID_PARAM = "messageOID";

    private static final class EventConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventOptions";

        private EventConfigurationDescriptor(){
            super(RESOURCE_NAME, EventConfiguration.class, SEVERITY_PARAM, MESSAGE_OID_PARAM);
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
                    SMART_MODE_KEY,
                    DISCOVERY_TIMEOUT_PROPERTY);
        }
    }

    private static final LazyReference<SnmpConnectorDescriptionProvider> INSTANCE = LazyReference.soft();

    private SnmpConnectorDescriptionProvider(){
        super(new ConnectorConfigurationDescriptor(),
                new AttributeConfigurationDescriptor(),
                new EventConfigurationDescriptor());
    }

    static SnmpConnectorDescriptionProvider getInstance(){
        return INSTANCE.get(SnmpConnectorDescriptionProvider::new);
    }

    static Duration getResponseTimeout(final AttributeDescriptor attributeParams){
        final long timeout = parseStringField(attributeParams, RESPONSE_TIMEOUT_PARAM, Long::parseLong).orElse(DEFAULT_RESPONSE_TIMEOUT);
        return Duration.ofMillis(timeout);
    }

    private static OctetString parseEngineID(final Map<String, String> parameters) {
        return getValue(parameters, ENGINE_ID_PARAM, OctetString::fromHexString).orElseGet(() -> new OctetString(MPv3.createLocalEngineID()));
    }

    private static OctetString parseCommunity(final Map<String, String> parameters){
        return getValue(parameters, COMMUNITY_PARAM, OctetString::new).orElseGet(() -> new OctetString("public"));
    }

    private static OID getAuthenticationProtocol(final String authProtocol){
        if(authProtocol == null || authProtocol.isEmpty()) return null;
        else switch (authProtocol.toLowerCase()){
            case "md-5":
            case "md5": return AuthMD5.ID;
            case "sha": return AuthSHA.ID;
            case "hmac128-sha224": return AuthHMAC128SHA224.ID;
            case "hmac192-sha256": return AuthHMAC192SHA256.ID;
            case "hmac256-sha384": return AuthHMAC256SHA384.ID;
            case "hmac384-sha512": return AuthHMAC384SHA512.ID;
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
        final ExecutorService threadPool = parseThreadPool(parameters);
        final OctetString userName = getValue(parameters, USER_NAME_PARAM, OctetString::new).orElse(null);
        final OID authProtocol = getValue(parameters, AUTH_PROTOCOL_PARAM, SnmpConnectorDescriptionProvider::getAuthenticationProtocol).orElse(null);
        final OctetString password = getValue(parameters, PASSWORD_PARAM, OctetString::new).orElse(null);
        final OID encryptionProtocol = getValue(parameters, ENCRYPTION_PROTOCOL_PARAM, SnmpConnectorDescriptionProvider::getEncryptionProtocol).orElse(null);
        final OctetString encryptionKey = getValue(parameters, ENCRYPTION_KEY_PARAM, OctetString::new).orElse(null);
        final Address localAddress = getValue(parameters, LOCAL_ADDRESS_PARAM, GenericAddress::parse).orElse(null);
        final OctetString securityContext = getValue(parameters, SECURITY_CONTEXT_PARAM, OctetString::new).orElse(null);
        final int socketTimeout = getValueAsInt(parameters, SOCKET_TIMEOUT_PARAM, Integer::parseInt).orElse(DEFAULT_SOCKET_TIMEOUT);

        return userName == null ?
                SnmpClient.create(connectionAddress, community, localAddress, socketTimeout, threadPool):
                SnmpClient.create(connectionAddress, engineID, userName, authProtocol, password, encryptionProtocol, encryptionKey, securityContext, localAddress, socketTimeout, threadPool);
    }

    Duration parseDiscoveryTimeout(final Map<String, String> configuration) {
        final long timeoutMillis = getValueAsLong(configuration, DISCOVERY_TIMEOUT_PROPERTY, Long::parseLong).orElse(DEFAULT_DISCOVERY_TIMEOUT);
        return Duration.ofMillis(timeoutMillis);
    }
}
