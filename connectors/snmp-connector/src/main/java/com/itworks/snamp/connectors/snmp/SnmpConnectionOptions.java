package com.itworks.snamp.connectors.snmp;

import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import java.io.IOException;
import java.util.Map;

import static com.itworks.snamp.connectors.snmp.SnmpConnectorConfigurationProvider.*;

/**
 * Represents SNMP client connection options.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpConnectionOptions {
    private final Address connectionAddress;
    private final OctetString engineID;
    private final OctetString community;
    private final OctetString userName;
    private final Address localAddress;
    private final OID authProtocol;
    private final OctetString password;
    private final OID encryptionProtocol;
    private final OctetString encryptionKey;
    private final OctetString securityContext;

    public SnmpConnectionOptions(final String connectionString, final Map<String, String> parameters) {
        connectionAddress = GenericAddress.parse(connectionString);
        engineID = parameters.containsKey(ENGINE_ID_PARAM) ?
                new OctetString(parameters.get(ENGINE_ID_PARAM)) :
                new OctetString(MPv3.createLocalEngineID());
        community = parameters.containsKey(COMMUNITY_PARAM) ?
                new OctetString(parameters.get(COMMUNITY_PARAM)) :
                new OctetString("public");
        userName = parameters.containsKey(USER_NAME_PARAM) ?
                new OctetString(parameters.get(USER_NAME_PARAM)) :
                null;
        authProtocol = getAuthenticationProtocol(parameters.containsKey(AUTH_PROTOCOL_PARAM) ?
                parameters.get(AUTH_PROTOCOL_PARAM) :
                null);
        password = parameters.containsKey(PASSWORD_PARAM) ?
                new OctetString(parameters.get(PASSWORD_PARAM)):
                null;
        encryptionProtocol = getEncryptionProtocol(parameters.containsKey(ENCRYPTION_PROTOCOL_PARAM)  ?
                parameters.get(ENCRYPTION_PROTOCOL_PARAM) :
                null);
        encryptionKey = parameters.containsKey(ENCRYPTION_KEY_PARAM) ?
                new OctetString(parameters.get(ENCRYPTION_KEY_PARAM)) :
                null;
        localAddress = parameters.containsKey(LOCAL_ADDRESS_PARAM) ?
                GenericAddress.parse(parameters.get(LOCAL_ADDRESS_PARAM)) :
                null;
        securityContext = parameters.containsKey(SECURITY_CONTEXT_PARAM) ?
                new OctetString(parameters.get(SECURITY_CONTEXT_PARAM)) :
                null;
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

    /**
     * Creates a new instance of SNMP client.
     * <p>
     *     Don't forget to call {@link org.snmp4j.Snmp#listen()} method on the return value.
     * </p>
     * @return A new instance of SNMP client.
     * @throws IOException Unable to instantiate SNMP client.
     */
    public SnmpClient createSnmpClient() throws IOException{
        return userName == null ?
                SnmpClient.create(connectionAddress, community, localAddress):
                SnmpClient.create(connectionAddress, engineID, userName, authProtocol, password, encryptionProtocol, encryptionKey, securityContext, localAddress);
    }

    public static boolean authenticationRequred(final Map<String, String> connectionOptions) {
        return connectionOptions.containsKey(ENGINE_ID_PARAM) ||
                connectionOptions.containsKey(USER_NAME_PARAM) ||
                connectionOptions.containsKey(PASSWORD_PARAM) ||
                connectionOptions.containsKey(ENCRYPTION_KEY_PARAM) ||
                connectionOptions.containsKey(ENCRYPTION_PROTOCOL_PARAM) ||
                connectionOptions.containsKey(AUTH_PROTOCOL_PARAM) ||
                connectionOptions.containsKey(SECURITY_CONTEXT_PARAM);
    }
}
