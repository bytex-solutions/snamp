package com.bytex.snamp.connector.modbus.transport;

import com.bytex.snamp.connector.modbus.master.*;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represents client (master) of Modbus protocol.
 * @author  Roman Sakno
 * @since 1.0
 * @version 2.1
 */
public interface ModbusMaster extends CoilAccess, Closeable, InputDiscreteAccess, InputRegisterAccess, HoldingRegisterAccess, FileAccess {
    void setRetryCount(final int value);
    void connect(final int socketTimeout) throws IOException;
}
