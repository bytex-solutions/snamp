package com.itworks.snamp.connectors.modbus.transport;

/**
 * Represents type of the transport for Modbus packets.
 */
public enum ModbusTransportType {
    TCP {
        @Override
        ModbusTcpClient createClient(final String address, final int port) {
            return new ModbusTcpClient(address, port);
        }
    },
    UDP {
        @Override
        ModbusUdpClient createClient(final String address, final int port) {
            return new ModbusUdpClient(address, port);
        }
    };

    /**
     * Creates a new instance of the Modbus master controller.
     * @param address An address of remote Modbus slave device.
     * @param port Incoming port on remote Modbus slave device.
     * @return A new instance of Modbus client.
     */
    abstract ModbusClient createClient(final String address, final int port);
}
