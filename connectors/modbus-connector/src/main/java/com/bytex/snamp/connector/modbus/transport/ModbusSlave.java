package com.bytex.snamp.connector.modbus.transport;

import com.bytex.snamp.connector.modbus.slave.*;

import java.io.Closeable;

/**
 * Represents Modbus slave device.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface ModbusSlave extends Closeable {
    ModbusSlave register(final int ref, final DigitalOutputAccessor output);
    ModbusSlave register(final int ref, final DigitalInputAccessor input);
    ModbusSlave register(final int ref, final InputRegisterAccessor input);
    ModbusSlave register(final int ref, final OutputRegisterAccessor output);
    ModbusSlave register(final int fileNumber, final FileRecordAccessor file);
    void setUnitID(final int value);
    boolean isListening();
    void listen();
}
