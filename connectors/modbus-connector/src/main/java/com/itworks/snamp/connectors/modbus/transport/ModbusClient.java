package com.itworks.snamp.connectors.modbus.transport;

import com.itworks.snamp.connectors.modbus.protocol.CoilAccess;
import com.itworks.snamp.connectors.modbus.protocol.HoldingRegisterAccess;
import com.itworks.snamp.connectors.modbus.protocol.InputDiscreteAccess;
import com.itworks.snamp.connectors.modbus.protocol.InputRegisterAccess;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents client (master) of Modbus protocol.
 * @author  Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface ModbusClient extends CoilAccess, Closeable, InputDiscreteAccess, InputRegisterAccess, HoldingRegisterAccess {
    /**
     * Connects to the Modbus slave device.
     * @throws IOException Network problems
     */
    void openConnection() throws IOException;

    boolean isReconnecting();
}
