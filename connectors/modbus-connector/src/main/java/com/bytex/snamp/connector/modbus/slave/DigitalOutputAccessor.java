package com.bytex.snamp.connector.modbus.slave;

/**
 * Represents digital output.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface DigitalOutputAccessor extends DigitalInputAccessor {
    void setValue(final boolean value);
}
