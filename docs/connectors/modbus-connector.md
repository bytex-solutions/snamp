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

You must familiar with Modbus standard for using this type of resource connector. Otherwise, it is highly recommended to read [Modbus Application Protocol](http://www.modbus.org/docs/Modbus_Application_Protocol_V1_1b.pdf) specification.

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

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
retryCount | Integer | No | Number of attemptions when sending data to the slave device. By default it is equal to 3 | `10`
connectionTimeout | Integer | No | Connection timeout in millis. By default it is equal to `2000` | `50000`

Any other parameters will be ignored.

## Configuring attributes
Each attribute configured in JMX Resource Connector has the following configuration schema:
* `Name` - one of the predefined names:
  * `coil` - read/write access to device coil (of type `bool` or `array(bool)`)
  * `inputRegister` - read-only access to the register of device (of type `int16` or `array(int16)`)
  * `inputDiscrete` - read-only accesso to the digital input of device (of type `bool` or `array(bool)`)
  * `holdingRegister` - read/write access to the register of device (of type `int16` or `array(int16)`)
  * `file` - read/write access to the recors of the file on device (of type `array(int16)`)
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
offset | Integer | Yes | Zero-based index of the coil/register/file on slave device | `0`
count | Integer | No | If this parameter is not specified then `coil`, `inputRegister`, `inputDiscrete` and `holdingRegister` will have a scalar data type. Otherwise, the attribute provides access to a range of registers/inputs on the device. In this case the type of the attribute will be an array. For `file` attribute this parameter means a number of records to read or write | `2`
unitID | Integer | No | ID of the slave device. By default it is equal to `0` | `7`
recordSize | Integer | Yes but for `file` attribute only | A number of registers in the single record | `5`

Any other parameters will be ignored.
