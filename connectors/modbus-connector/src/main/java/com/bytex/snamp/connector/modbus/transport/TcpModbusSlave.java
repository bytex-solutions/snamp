package com.bytex.snamp.connector.modbus.transport;

import com.ghgande.j2mod.modbus.net.ModbusTCPListener;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class TcpModbusSlave extends AbstractModbusSlave<ModbusTCPListener> {

    TcpModbusSlave(final int port) {
        super(new ModbusTCPListener(5));
        listener.setPort(port);

    }
}
