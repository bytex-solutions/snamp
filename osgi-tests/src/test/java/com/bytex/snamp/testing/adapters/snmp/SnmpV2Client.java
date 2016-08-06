package com.bytex.snamp.testing.adapters.snmp;

import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

/**
 * Simple SNMPv2 client
 * @author Evgeniy Kirichenko
 */
final class SnmpV2Client extends AbstractSnmpClient {

    /**
     * Snmp version 2 constructor
     * @param address
     */
    SnmpV2Client(String address) throws IOException {
        this.address = address;
        start();
    }

    /**
     * Start the Snmp session. If you forget the listen() method you will not
     * get any answers because the communication is asynchronous
     * and the listen() method listens for answers.
     * @throws java.io.IOException
     */
    @SuppressWarnings("unchecked")
    private void start() throws IOException {
        final MessageDispatcherImpl dispatcher = new MessageDispatcherImpl();
        dispatcher.addMessageProcessingModel(new MPv2c());
        transport = new DefaultUdpTransportMapping();
        transport.listen();
        snmp = new Snmp(dispatcher, transport);
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
