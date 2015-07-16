package com.itworks.snamp.connectors.modbus.protocol;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.Register;

/**
 * Provides access to holding registers.
 */
public interface HoldingRegisterAccess {
    Register[] readHoldingRegisters(final int ref, final int count) throws ModbusException;

    Register readHoldingRegister(final int ref) throws ModbusException;

    void writeHoldingRegister(final int ref, final Register register) throws ModbusException;
}
