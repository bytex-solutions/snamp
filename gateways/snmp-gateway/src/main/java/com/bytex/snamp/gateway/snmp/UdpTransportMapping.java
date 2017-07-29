package com.bytex.snamp.gateway.snmp;

import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.WorkerTask;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * Fixed version of {@link DefaultUdpTransportMapping}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class UdpTransportMapping extends DefaultUdpTransportMapping {
    UdpTransportMapping(final UdpAddress udpAddress) throws IOException {
        super(udpAddress);
    }

    @Override
    public void close() throws IOException { //Fix: Address already in use
        DatagramSocket closingSocket = socket;
        if (closingSocket != null)
            try {
                closingSocket.close();
            } finally {
                socket = null;
            }
        final WorkerTask task = listener;
        if (task != null) {
            task.interrupt();
            listener = null;
        }
    }
}
