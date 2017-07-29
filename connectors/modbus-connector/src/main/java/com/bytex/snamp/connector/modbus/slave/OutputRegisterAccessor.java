package com.bytex.snamp.connector.modbus.slave;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface OutputRegisterAccessor extends InputRegisterAccessor {
    void setValue(final short value);
}
