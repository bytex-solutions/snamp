package com.itworks.snamp.connectors.modbus.protocol;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * Provides access to coins.
 */
public interface CoilAccess extends SlaveDeviceAccess {
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
     * @param ref    the offset of the coil to be written.
     * @param state  the coil state to be written.
     * @return the state of the coil as returned from the slave.
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    boolean writeCoil(final int ref, final boolean state) throws ModbusException;

    void writeCoils(int ref, BitVector coils) throws ModbusException;
}
