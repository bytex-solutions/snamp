package com.itworks.snamp.connectors.modbus.transport;

import com.itworks.snamp.connectors.modbus.master.CoilAccess;
import com.itworks.snamp.connectors.modbus.master.HoldingRegisterAccess;
import com.itworks.snamp.connectors.modbus.master.InputDiscreteAccess;
import com.itworks.snamp.connectors.modbus.master.InputRegisterAccess;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents client (master) of Modbus protocol.
 * @author  Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface ModbusMaster extends CoilAccess, Closeable, InputDiscreteAccess, InputRegisterAccess, HoldingRegisterAccess {
    void setRetryCount(final int value);
    void connect(final int socketTimeout) throws IOException;
}
