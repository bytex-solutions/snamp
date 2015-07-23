package com.itworks.snamp.connectors.modbus.slave;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface OutputRegisterAccessor extends InputRegisterAccessor {
    void setValue(final short value);
}
