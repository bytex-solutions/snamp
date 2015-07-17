package com.itworks.snamp.connectors.modbus.protocol;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

/**
 * Provides access to input registers.
 */
public interface InputRegisterAccess extends SlaveDeviceAccess {
    InputRegister[] readInputRegisters(final int ref, final int count) throws ModbusException;

    InputRegister readInputRegister(final int ref) throws ModbusException;
}
