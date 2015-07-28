package com.bytex.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.net.ModbusTCPListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class TcpModbusSlave extends AbstractModbusSlave<ModbusTCPListener> {

    TcpModbusSlave(final int port) {
        super(new ModbusTCPListener(5));
        listener.setPort(port);

    }
}
