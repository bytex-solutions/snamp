package com.bytex.snamp.connectors.modbus.transport;

import com.ghgande.j2mod.modbus.net.ModbusUDPListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UdpModbusSlave extends AbstractModbusSlave<ModbusUDPListener> {
    UdpModbusSlave(final int port){
        super(new ModbusUDPListener());
        listener.setPort(port);
    }
}
