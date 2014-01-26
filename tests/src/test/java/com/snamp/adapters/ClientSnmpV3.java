package com.snamp.adapters;

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
public class ClientSnmpV3 extends AbstractSnmpClient {

    private final String username;

    /**
     * Create usual SNMP client for snmpv3 with auth
     * @param address
     */
    public ClientSnmpV3(final String address, final String username, final String password) {
        this.address = address;
        this.username = username;
        try {
            start(username, password);
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
    private void start(final String username, final String password) throws IOException {
        //setup transport
        //setup transport
        snmp = new Snmp(transport = new DefaultUdpTransportMapping());
        snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
        // Security protocol.
        SecurityProtocols securityProtocols = SecurityProtocols.getInstance();
        securityProtocols.addAuthenticationProtocol(new AuthMD5());
        //USM
        final USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        snmp.getUSM().addUser(new OctetString(username), new UsmUser(new OctetString(username), AuthMD5.ID, new OctetString(password), null, null));
        snmp.setLocalEngine(MPv3.createLocalEngineID(), 0, 0);
        final USM s = snmp.getUSM();
        transport.listen();
    }

    /**
     * SnmpV2 Target based on UserModelCommunity
     * @return
     */

    protected UserTarget getTarget() {
        final UserTarget target = new UserTarget();
        final Address targetAddress = GenericAddress.parse(address);
        target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV); //SecurityLevel.AUTH_NOPRIV
        target.setSecurityName(new OctetString(username));
        target.setAuthoritativeEngineID(MPv3.createLocalEngineID());
        target.setSecurityModel(SecurityModel.SECURITY_MODEL_USM);
        target.setAddress(targetAddress);
        target.setRetries(3);
        target.setTimeout(5000000);
        target.setVersion(SnmpConstants.version3);
        return target;
    }
}
