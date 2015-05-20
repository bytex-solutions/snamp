SNMP Resource Connector
====
SNMP Resource Connector allows to manage and monitor managed resources (software components, networks switchers and routers) accessible through SNMPv2/SNMPv3 protocol.
> SNMPv1 protocol is not supported. UDP is the only supported transport protocol. `TCP` binding is not supported.

Short list of supported features:

Feature name | Description
---- | ----
Attributes | Each SNMP Managed Object with unqiue OID will be accessible as attribute. Accessing to attributes is implemented via SNMP GET/SET
Notifications | Converting SNMP Traps to SNAMP notifications

Note that this connector utilizes **its own internal thread pool that can be configured explicitly**.

## Connection String
SNMP Resource Connector requires an address of the SNMP Agent acting as a managed resource. This address has the following format:
```
udp://<ip-address>/<port>
```

It is recommended to use IPv4 or IPv6 address instead of Host Name to avoid DNS resolving problems.

On Linux, you may use `snmpwalk` to test SNMP connection. Make sure that SNMP Agent is SNMPv2/SNMPv3 compatible.

Examples of connection string:
* `udp://127.0.0.1/161`
* `udp://192.168.0.1/9293`

## Configuration Parameters
SNMP Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
community | String | true (for SNMPv2) | The _SNMP Community string_ is like a user id or password and allowed for SNMPv2 only | `public`
engineID | HEX | true (for SNMPv3) | Authoritative engine ID (for SNMPv3 only) in hexadecimal format | `80:00:13:70:01:7f:00:01:01:be:1e:8b:35`
userName | String | true (for SNMPv3) | Security name used for authentication on SNMPv3 agent
authenticationProtocol | Enum | true (for SNMPv3) | Authentication protocol (password hashing algorithm) | `sha`
encryptionProtocol | Enum | true (for SNMPv3) | SNMP packet encryption protocol (payload encryption algorithm) | `aes128`
password | String | true (for SNMPv3) | Password used to authenticate on SNMPv3 agent | `pwd`
encryptionKey | String | true (for SNMPv3) | Secret string used as a encryption key for symmetric encryption algorithm specified in `encryptionProtocol` parameter | `secret`
securityContext | String | false | The context name of the scoped PDU (for SNMPv3 only) | `context`
socketTimeout | Integer | false | UDP socket timeout, in millis. It is used as a maximum time interval for receiving and sending PDU packets over network. This parameter must be specified if your network has high latency | `2000`
localAddress | `udp://<ip-address>/<port>` | false | UDP outgoing address and port. Usually, you should not specify this parameter. But it is very useful for testing purposes when you QA team wants to capture data packet traces between SNAMP and SNMP agent | `udp://127.0.0.1/44495`

Note that resource connector cannot determine SNMP protocol version automatically. Moreover, it cannot automatically discover values of security parameters such as `community`, `authenticationProtocol`, `userName`, `password` and etc.

### SNMPv2 configuration
SNMP Resource Connector will choose SNMPv2 protocol if `userName` configuration parameter is undefined. In this case only `community` configuration parameter affecting SNMPv2 communication process. Any other SNMPv3-specific parameters will be ignored.

### SNMPv3 configuration
SNMP Resource Connector will choose SNMPv3 protocol if `userName` configuration parameter is defined. The value of `community` parameter will be ignored. But `userName` is not the only required configuration parameter for SNMPv3 communication.

### Authentication protocol
SNMP Resource Connector supports the following authentication protocols:

Enum value | Description
---- | ----
md5 | The password will be hashed using MD5 algorithm
sha | The password will be hashed using SHA algorithm

Note that parameters related to thread pool is omitted. See **Configuration** page for more information about thread pool configuration.

### Encryption protocol
SNMP Resource Connector supports the following encryption protocols:

Enum value | Description
---- | ----
aes128 | [Advanced Encryption Standard](http://en.wikipedia.org/wiki/Advanced_Encryption_Standard) with 128-bit key strength
aes192 | [Advanced Encryption Standard](http://en.wikipedia.org/wiki/Advanced_Encryption_Standard) with 192-bit key strength
aes256 | [Advanced Encryption Standard](http://en.wikipedia.org/wiki/Advanced_Encryption_Standard) with 256-bit key strength
des | [Data Encryption Standard](http://en.wikipedia.org/wiki/Data_Encryption_Standard)
3des | [Triple Data Encryption Algorithm](http://en.wikipedia.org/wiki/Triple_DES)

## Configuring attributes
SNMP Resource Connector interprets SNMP Managed Object as an attribute. Each SNMP Managed Object identified by OID (OBject Identifier). Structurally, an OID consists of a node in a hierarchically-assigned namespace, formally defined using the ITU-T's ASN.1 standard, [X.690](http://en.wikipedia.org/wiki/X.690).

Each attribute configured in SNMP Resource Connector has the following configuration schema:

* `Name` - OID of the SNMP Managed Object (for example, `1.4.5.7.5.9.0`)
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
snmpConversionFormat | Enum | false | Specifies mapping between ASN.1 and SNAMP type system | `text`
responseTimeout | Integer | false | Specifies timeout (in millis) when waiting SNMP GET/SET response from agent | `2000`

### Conversion format
Some ASN.1 data types can be mapped into more than one SNAMP data type (see **SNAMP Management Information Model** for more information). This behavior can be specified manually using `snmpConversionFormat` configuration parameter.

The following table describes ASN.1 mapping for different conversion formats:

ASN.1 data type | Conversion format | SNAMP MIM data type
---- | ---- | ----
OCTET_STRING | _Default_ (if not specified) | The connector will choose `text` or `hex` automatically if string containing in Managed Object is human-readable
OCTET_STRING | text | string (human-readable)
OCTET_STRING | hex | string (in HEX format, for example `8d:65:32:57:f6`)
OCTET_STRING | raw | int8 array
OID | _Default_ (if not specified) | int16 array
OID | text | string (in dotted notation, for example `1.5.10.0`)
OID | raw | int16 array
TIME_TICKS | _Default_ (if not specified) | int64
TIME_TICKS | text | string
TIME_TICKS | raw | int64
IP_ADDRESS | _Default_ (if not specified) | int8 array
IP_ADDRESS | text | string (human-readable address, for example `127.0.0.1`)
IP_ADDRESS | raw | int8 array