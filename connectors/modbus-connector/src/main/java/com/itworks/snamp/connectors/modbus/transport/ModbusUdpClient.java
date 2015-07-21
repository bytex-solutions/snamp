package com.itworks.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.io.ModbusTransaction;
import com.ghgande.j2mod.modbus.io.ModbusUDPTransaction;
import com.ghgande.j2mod.modbus.net.UDPMasterConnection;
import com.ghgande.j2mod.modbus.net.UDPTerminal;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Represents UDP transport for Modbus.
 * This class cannot be inherited.
 */
final class ModbusUdpClient extends AbstractModbusClient {
    private final UDPMasterConnection connection;

    ModbusUdpClient(final InetAddress addr, final int port){
        connection = new UDPMasterConnection(addr);
        connection.setPort(port);
    }

    ModbusUdpClient(final String addr, final int port) throws UnknownHostException {
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
        final ModbusUDPTransaction transaction = new ModbusUDPTransaction();
        transaction.setCheckingValidity(true);
        return transaction;
    }

    @Override
    public void close() {
        connection.close();
    }
}
