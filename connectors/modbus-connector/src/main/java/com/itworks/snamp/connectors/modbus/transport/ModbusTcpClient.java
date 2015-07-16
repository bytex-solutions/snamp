package com.itworks.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.IOException;
import java.util.Arrays;

/**
 * Represents TCP transport for Modbus.
 * This class cannot be inherited.
 */
final class ModbusTcpClient extends ModbusTCPMaster implements ModbusClient {
    ModbusTcpClient(final String addr, final int port) {
        super(addr, port);
    }

    @Override
    public void openConnection() throws IOException {
        try {
            connect();
        }
        catch (final IOException e){
            throw e;
        }
        catch (final Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean readCoil(final int ref) throws ModbusException {
        final BitVector vector = readCoils(ref, 1);
        if(vector == null || vector.size() == 0)
            throw new ModbusException("Unexpected bit vector in response from Modbus slave device");
        else return vector.getBit(0);
    }

    @Override
    public void writeCoils(final int ref, final BitVector coils) throws ModbusException {
        writeMultipleCoils(ref, coils);
    }

    @Override
    public boolean readInputDiscrete(final int ref) throws ModbusException {
        final BitVector vector = readInputDiscretes(ref, 1);
        if(vector == null || vector.size() == 0)
            throw new ModbusException("Unexpected bit vector in response from Modbus slave device");
        else return vector.getBit(0);
    }

    @Override
    public InputRegister readInputRegister(final int ref) throws ModbusException {
        final InputRegister[] result = readInputRegisters(ref, 1);
        if(result == null || result.length != 1)
            throw new ModbusException("Unrecognized response from Modbus slave device. Received registers: " + Arrays.toString(result));
        else return result[0];
    }

    @Override
    public Register[] readHoldingRegisters(final int ref, final int count) throws ModbusException {
        return readMultipleRegisters(ref, count);
    }

    @Override
    public Register readHoldingRegister(final int ref) throws ModbusException {
        final Register[] result = readHoldingRegisters(ref, 1);
        if(result == null || result.length != 1)
            throw new ModbusException("Unrecognized response from Modbus slave. Received registers: " + Arrays.toString(result));
        else return result[0];
    }

    @Override
    public void writeHoldingRegister(final int ref, final Register register) throws ModbusException {
        writeMultipleRegisters(ref, new Register[]{register});
    }

    @Override
    public void close() {
        disconnect();
    }
}
