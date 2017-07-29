package com.bytex.snamp.connector.modbus.transport;

import com.ghgande.j2mod.modbus.net.ModbusUDPListener;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class UdpModbusSlave extends AbstractModbusSlave<ModbusUDPListener> {
    UdpModbusSlave(final int port){
        super(new ModbusUDPListener());
        listener.setPort(port);
    }
}
