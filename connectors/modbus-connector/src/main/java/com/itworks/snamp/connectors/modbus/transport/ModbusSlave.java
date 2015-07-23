package com.itworks.snamp.connectors.modbus.transport;

import com.itworks.snamp.connectors.modbus.slave.DigitalInputAccessor;
import com.itworks.snamp.connectors.modbus.slave.DigitalOutputAccessor;
import com.itworks.snamp.connectors.modbus.slave.InputRegisterAccessor;
import com.itworks.snamp.connectors.modbus.slave.OutputRegisterAccessor;

import java.io.Closeable;

/**
 * Represents Modbus slave device.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ModbusSlave extends Closeable {
    ModbusSlave register(final int ref, final DigitalOutputAccessor output);
    ModbusSlave register(final int ref, final DigitalInputAccessor input);
    ModbusSlave register(final int ref, final InputRegisterAccessor input);
    ModbusSlave register(final int ref, final OutputRegisterAccessor output);
    void setUnitID(final int value);
    boolean isListening();
    void listen();
}
