package com.snamp.adapters;

import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Simple SNMPv2 client
 * @author Evgeniy Kirichenko
 */
public final class SnmpV2Client extends AbstractSnmpClient {

    /**
     * Snmp version 2 constructor
     * @param address
     */
    public SnmpV2Client(String address) {
        this.address = address;
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
        transport = new DefaultUdpTransportMapping();
        transport.listen();
        snmp = new Snmp(transport);
    }

    /**
     * SnmpV3 Target based on UserModelCommunity
     * @return
     */
    protected CommunityTarget getTarget() {
        final CommunityTarget target = new CommunityTarget();
        final Address targetAddress = GenericAddress.parse(address);
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(3);
        target.setTimeout(5000000);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }
}
