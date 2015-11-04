package com.bytex.snamp.connectors.modbus.transport;

import com.bytex.snamp.SafeCloseable;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransaction;
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents UDP transport for Modbus.
 * This class cannot be inherited.
 */
final class UdpModbusMaster extends AbstractModbusMaster implements SafeCloseable {
    private final UDPMasterConnection connection;

    UdpModbusMaster(final InetAddress addr, final int port){
        connection = new UDPMasterConnection(addr);
        connection.setPort(port);
    }

    UdpModbusMaster(final String addr, final int port) throws UnknownHostException {
        this(InetAddress.getByName(addr), port);
    }

    @Override
    public void connect(final int socketTimeout) throws IOException {
        try {
            connection.connect();
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    protected ModbusUDPTransaction createTransaction() {
        final ModbusUDPTransaction transaction = new ModbusUDPTransaction(connection);
        transaction.setCheckingValidity(true);
        return transaction;
    }

    @Override
    public void close() {
        connection.close();
    }
}
