package com.itworks.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Represents TCP transport for Modbus.
 * This class cannot be inherited.
 */
final class ModbusTcpClient extends AbstractModbusClient {
    private final TCPMasterConnection connection;

    ModbusTcpClient(final InetAddress addr, final int port){
        connection = new TCPMasterConnection(addr);
        connection.setPort(port);
    }

    ModbusTcpClient(final String addr, final int port) throws UnknownHostException {
        this(InetAddress.getByName(addr), port);
    }

    @Override
    public void connect(final int socketTimeout) throws IOException {
        connection.setTimeout(socketTimeout);
        try {
            connection.connect();
        }
        catch (final IOException e){
            throw e;
        }
        catch (final Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    protected ModbusTCPTransaction createTransaction() {
        final ModbusTCPTransaction transaction = new ModbusTCPTransaction();
        transaction.setCheckingValidity(true);
        transaction.setReconnecting(true);
        return transaction;
    }


    @Override
    public void close() {
        connection.close();
    }
}
