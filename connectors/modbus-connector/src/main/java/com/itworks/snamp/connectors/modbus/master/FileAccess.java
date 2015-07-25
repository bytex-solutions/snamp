package com.itworks.snamp.connectors.modbus.master;

import com.ghgande.j2mod.modbus.ModbusException;

/**
 * Provides access to the file records.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface FileAccess extends SlaveDeviceAccess {
    short[] readFile(final int unitID,
                     final int file,
                     final int recordCount,
                     final int recordSize) throws ModbusException;
    void writeFile(final int unitID,
                   final int file,
                   final int recordSize,
                   final short[] value) throws ModbusException;
}
