package com.bytex.snamp.connectors.modbus.slave;

/**
 * Represents digital output.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface DigitalOutputAccessor extends DigitalInputAccessor {
    void setValue(final boolean value);
}
