package com.itworks.snamp.connectors.modbus.master;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.util.BitVector;

/**
 * Provides access to input discrete.
 */
public interface InputDiscreteAccess extends SlaveDeviceAccess {
    BitVector readInputDiscretes(final int unitID, final int ref, final int count) throws ModbusException;

    boolean readInputDiscrete(final int unitID, final int ref) throws ModbusException;
}
