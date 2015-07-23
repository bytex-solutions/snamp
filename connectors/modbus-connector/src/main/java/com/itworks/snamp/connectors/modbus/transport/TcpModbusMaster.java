package com.itworks.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.io.ModbusTransport;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransport;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents TCP transport for Modbus.
 * This class cannot be inherited.
 */
final class TcpModbusMaster extends AbstractModbusMaster {
    private final TCPMasterConnection connection;
    private final boolean headless;

    TcpModbusMaster(final InetAddress addr, final int port, final boolean headless){
        connection = new TCPMasterConnection(addr);
        connection.setPort(port);
        this.headless = headless;
    }

    TcpModbusMaster(final String addr, final int port, final boolean headless) throws UnknownHostException {
        this(InetAddress.getByName(addr), port, headless);
    }

    @Override
    public void connect(final int socketTimeout) throws IOException {
        try {
            connection.connect();
        }
        catch (final IOException e){
            throw e;
        }
        catch (final Exception e) {
            throw new IOException(e);
        }
        connection.setTimeout(socketTimeout);
        final ModbusTransport transport = connection.getModbusTransport();
        if(headless && transport instanceof ModbusTCPTransport)
            ((ModbusTCPTransport)transport).setHeadless();
    }

    @Override
    protected ModbusTCPTransaction createTransaction() {
        final ModbusTCPTransaction transaction = new ModbusTCPTransaction(connection);
        transaction.setCheckingValidity(true);
        transaction.setReconnecting(true);
        return transaction;
    }

    @Override
    public void close() {
        connection.close();
    }
}
