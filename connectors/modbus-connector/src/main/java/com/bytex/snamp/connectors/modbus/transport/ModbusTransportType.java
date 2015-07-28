package com.bytex.snamp.connectors.modbus.transport;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Represents type of the transport for Modbus packets.
 */
public enum ModbusTransportType {
    TCP {
        @Override
        public TcpModbusMaster createMaster(final String address, final int port) throws UnknownHostException {
            return new TcpModbusMaster(address, port, false);
        }

        @Override
        public TcpModbusSlave createSlave(final int port) {
            return new TcpModbusSlave(port);
        }
    },
    UDP {
        @Override
        public UdpModbusMaster createMaster(final String address, final int port) throws UnknownHostException {
            return new UdpModbusMaster(address, port);
        }

        @Override
        public UdpModbusSlave createSlave(final int port) {
            return new UdpModbusSlave(port);
        }
    },
    RTU_IP{
        @Override
        public TcpModbusMaster createMaster(final String address, final int port) throws IOException {
            return new TcpModbusMaster(address, port, true);
        }

        @Override
        public TcpModbusSlave createSlave(final int port) {
            return new TcpModbusSlave(port);
        }
    };

    /**
     * Creates a new instance of the Modbus master controller.
     * @param address An address of remote Modbus slave device.
     * @param port Incoming port on remote Modbus slave device.
     * @return A new instance of Modbus client.
     * @throws IOException Unable to instantiate Modbus master.
     */
    public abstract ModbusMaster createMaster(final String address, final int port) throws IOException;

    public abstract ModbusSlave createSlave(final int port);
}
