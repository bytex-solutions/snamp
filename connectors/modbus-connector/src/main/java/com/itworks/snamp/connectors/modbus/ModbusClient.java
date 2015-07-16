package com.itworks.snamp.connectors.modbus;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents client (master) of Modbus protocol
 */
interface ModbusClient extends Closeable {
    /**
     * Connects to the Modbus slave device.
     * @throws IOException Network problems
     */
    void connect() throws IOException;

    /**
     * Reads a given number of coil states from the slave.
     * <p/>
     * Note that the number of bits in the bit vector will be
     * forced to the number originally requested.
     *
     * @param ref   the offset of the coil to start reading from.
     * @param count the number of coil states to be read.
     * @return a <tt>BitVector</tt> instance holding the
     *         received coil states.
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    BitVector readCoils(final int ref, final int count) throws ModbusException;

    boolean readCoil(final int ref) throws ModbusException;

    /**
     * Writes a coil state to the slave.
     *
     * @param unitid the slave unit id.
     * @param ref    the offset of the coil to be written.
     * @param state  the coil state to be written.
     * @return the state of the coil as returned from the slave.
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    void writeCoil(final int unitid, final int ref, final boolean state) throws ModbusException;

    void writeCoils(int ref, BitVector coils) throws ModbusException;

    BitVector readInputDiscretes(final int ref, final int count) throws ModbusException;

    boolean readInputInputDiscrete(final int ref) throws ModbusException;

    InputRegister[] readInputRegisters(final int ref, final int count) throws ModbusException;

    short readInputRegister(final int ref) throws ModbusException;

    Register[] readHoldingRegisters(final int ref, final int count) throws ModbusException;

    Register readHoldingRegister(final int ref) throws ModbusException;

    void writeHoldingRegister(final int ref, final Register register) throws ModbusException;
}
