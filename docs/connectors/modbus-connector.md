Modbus Resource Connector
====
Modbus Resource Connector acting as Modbus Master device that can be connected to Modbus slave devices using TCP or UDP protocol directly or through serial device server (for example, a Moxa 5130). It is possible to collection information from registers and coils of the device. A full set of supported protocols:
* Modbus-UDP
* Modbus-TCP
* Modbus-RTU/IP

> Serial port is not supported

A list of things that can handled by the connector:
* **Coil** as read/write attribute of `bool` data type
* **Input Discrete** as read-only attribute of `bool` data type
* **Input Register** as read-only attribute of `int16` data type
* **Holding Register** as read/write attribute if `int16` data type

> Modbus Resource Connector doesn't support events or operations

Also, a range of coils/registers can be accessed using a single attribute.

## Connection String
Modbus Resource Connector uses URL format to establish connection between SNAMP and Modbus slave device:

```
<protocol>://<slave-device-name>:<port>
```

Supported _protocols_ are:
* `tcp` - Modbus/TCP protocol
* `udp` - Modbus/UDP protocol
* `rtu-ip` - SNAMP builds a Modbus-RTU message, including checksum, and then sends it over a TCP/IP connection to the gateway device, which puts the message on the Modbus wire -- the server takes any modbus messages it receives over the modbus wire and sends them back over the TCP connection to the SNAMP

_Slave device name_ must be a valid DNS-name of the Modbus Slave or Gateway device. _Port_ must be a valid socket port in slave/gateway side.

Examples:
* `tcp://light-switcher:3232`
* `rtu-ip://moxa-5130`

## Configuration Parameters
Modbus Resource Connector recognizes the following parameters:
