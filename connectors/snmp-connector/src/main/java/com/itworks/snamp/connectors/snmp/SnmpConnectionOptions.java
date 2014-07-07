package com.itworks.snamp.connectors.snmp;

import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
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
        localAddress = parameters.containsKey(LOCAL_ADDRESS_PARAM) ?
                GenericAddress.parse(parameters.get(LOCAL_ADDRESS_PARAM)) :
                null;
    }

    public int getSnmpVersion(){
        return userName != null ? SnmpConstants.version3 : SnmpConstants.version2c;
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
                SnmpClient.createClient(connectionAddress, community, localAddress):
                null;
    }
}
