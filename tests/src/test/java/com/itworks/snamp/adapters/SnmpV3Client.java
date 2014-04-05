package com.itworks.snamp.adapters;

import org.snmp4j.*;
import org.snmp4j.mp.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Client for snmpV3
 */
public final class SnmpV3Client extends AbstractSnmpClient {

    private final String username;
    private final SecurityLevel level;

    /**
     * Create usual SNMP client for snmpv3 with auth
     * @param address
     */
    public SnmpV3Client(final String address, final String username, final SecurityLevel security) {
        this.address = address;
        this.username = username;
        this.level = security;
        try {
            start();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Snmp client initialization error: " + e.getLocalizedMessage());
        }
    }
    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     * @throws java.io.IOException
     */
    private void start() throws IOException {
        //setup transport
        snmp = new Snmp(transport = new DefaultUdpTransportMapping());
        //BANANA: need to change the local engine ID, because if it is equal to USM engine ID
        //the security engine ID will be omitted
        ((MPv3)snmp.getMessageDispatcher().getMessageProcessingModel(MessageProcessingModel.MPv3)).setLocalEngineID(new OctetString("blah-blah").toByteArray());
        transport.listen();
    }

    /**
     * SnmpV2 Target based on UserModelCommunity
     * @return
     */



    protected UserTarget getTarget() {

        final UserTarget target = new UserTarget();
        final Address targetAddress = GenericAddress.parse(address);
        target.setAuthoritativeEngineID(MPv3.createLocalEngineID());
        target.setSecurityLevel(level.getSnmpValue());
        target.setSecurityName(new OctetString(username));
        target.setSecurityModel(SecurityModel.SECURITY_MODEL_USM);
        target.setAddress(targetAddress);
        target.setRetries(0);
        target.setTimeout(5000);
        target.setVersion(SnmpConstants.version3);
        return target;
    }
}
