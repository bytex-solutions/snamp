package com.itworks.snamp.connectors.modbus.master;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.Register;

/**
 * Provides access to holding registers.
 */
public interface HoldingRegisterAccess extends SlaveDeviceAccess {
    Register[] readHoldingRegisters(final int unitID, final int ref, final int count) throws ModbusException;

    Register readHoldingRegister(final int unitID, final int ref) throws ModbusException;

    void writeHoldingRegister(final int unitID, final int ref, final Register register) throws ModbusException;

    void writeHoldingRegisters(final int unitID, final int ref, final Register[] regs) throws ModbusException;
}
