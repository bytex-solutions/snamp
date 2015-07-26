package com.bytex.snamp.testing.adapters.snmp;

import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

/**
 * Client for snmpV3
 */
final class SnmpV3Client extends AbstractSnmpClient {

    private final String username;
    private final SecurityLevel level;
    private final OID authenticationProtocol;
    private final OctetString password;
    private final OID privacyProtocol;
    private final OctetString privacyKey;
    private final OctetString authoritativeEngineID;

    SnmpV3Client(final String engineID,
                 final String address,
                 final String username,
                 final SecurityLevel security,
                 final OID authenticationProtocol,
                 final String password,
                 final OID privacyProtocol,
                 final String privacyKey) throws IOException {
        this.authoritativeEngineID = OctetString.fromHexString(engineID);
        this.address = address;
        this.username = username;
        this.level = security;
        this.authenticationProtocol = authenticationProtocol;
        this.password = password == null || password.isEmpty() ? null : new OctetString(password);
        this.privacyProtocol = privacyProtocol;
        this.privacyKey = privacyKey == null || privacyKey.isEmpty() ? null : new OctetString(privacyKey);
        start();
    }

    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     * @throws java.io.IOException
     */
    private void start() throws IOException {
        //setup dispatcher
        final MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
        //BANANA: need to change the local engine ID, because if it is equal to USM engine ID
        //the security engine ID will be omitted
        final USM users = new USM(DefaultSecurityProtocols.getInstance(),
                new OctetString("blah-blah"),
                0);
        users.addUser(new OctetString(username), authoritativeEngineID, new UsmUser(new OctetString(username),
                authenticationProtocol,
                password,
                privacyProtocol,
                privacyKey));
        final MPv3 messageProcessingModel = new MPv3(users);
        dispatcher.addMessageProcessingModel(messageProcessingModel);
        //setup transport
        snmp = new Snmp(dispatcher, transport = new DefaultUdpTransportMapping());
        transport.listen();
    }

    protected UserTarget getTarget() {
        final UserTarget target = new UserTarget();
        final Address targetAddress = GenericAddress.parse(address);
        target.setAuthoritativeEngineID(authoritativeEngineID.getValue());
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
