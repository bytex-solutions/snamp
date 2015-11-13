MQ Resource Connector
====
MQ Resource Connector is very similar to [MDA Connector](mda-connector.md). MQ Connector accepts messages via JMS interface or AMQP protocol. These messages may be emitted by remote Agents. It is possible to set attributes and send notifications through this connector using any JMS-compliant or AMQP-compliant Messaging Middleware.

![Communication Scheme](mda-connector.png)

There are many use cases for this connector:
* As a monitor for [Invalid Message Queue](http://www.enterpriseintegrationpatterns.com/patterns/messaging/InvalidMessageChannel.html)
* As a monitor for [Dead Letter Queue](http://www.enterpriseintegrationpatterns.com/patterns/messaging/DeadLetterChannel.html)
* Analyze business messages on-the-fly when listening message queue
* As performance data collector
* As log events collector

MQ Resource Connector supports the following MQs out of the box:

* [ActiveMQ](http://activemq.apache.org/)
* IBM WebSphere MQ
* [Qpid Broker](https://qpid.apache.org/components/java-broker/)
* [RabbitMQ](https://www.rabbitmq.com/)
* Any [JMS](https://en.wikipedia.org/wiki/Java_Message_Service)-compliant Messaging Middleware
* [AMQP](https://en.wikipedia.org/wiki/Advanced_Message_Queuing_Protocol)-compliant Messaging Middleware

> Supported versions of AMQP protocol: 0-8, 0-9, 0-9-1, 0-10

The followings features are supported:

Feature | Comments
---- | ----
Attributes | New value can be delivered as Message via Message Queue. Also, it is possible to read stored value from the connector using MQ
Events | Can be delivered as Message via Message Queue

You can use dedicated Message Queue for delivering attributes and events, or using existing Message Queue to process and extract information from the business messages on-the-fly. For the first case, MQ Resource Connector declares its own message convention and message converter. For the second case it is possible to write custom converter using Groovy Script.

## Connection String
MQ Resource Connector supports the following schemas:

* **AMQP** scheme used to establish connection with RabbitMQ
* **ActiveMQ** scheme used to establish connection with ActiveMQ
* **JNDI** scheme used to establish connection with any JMS-compliant Messaging Middleware

### ActiveMQ support
ActiveMQ connection string has the following format:

```
activemq:<transport>://<host>[:<port>][/?<key=value>]
```

where _transport_:

Transport | Description | Example of connection string
---- | ---- | ----
vm | Client connect to each other within the same JVM | activemq:vm://localhost
tcp | ActiveMQ-specific wire protocol on top of TCP/IP stack. The connection is insecure | `activemq:tcp://localhost:61616`
ssl | ActiveMQ-specific wire protocol on top of TCP/IP stack. The connection is secure | `activemq:ssl://localhost`
http | ActiveMQ-specific wire protocol implemented on top of HTTP protocol. The connection is insecure | `activemq:http://localhost:61617`
udp | ActiveMQ-specific wire protocol implemented on top of UDP protocol. The connection is insecure | `activemq:udp://localhost`

For full documentation about ActiveMQ URL format, see [ActiveMQ Connection URL](http://activemq.apache.org/uri-protocols.html).

### AMQP support
AMQP connection string has the following format:

```
amqp://[<user>:<pass>@][<clientid>]<virtualhost>[?brokerlist='tcp://<host>:<port>'[&<option>='<value>']]
```

Connection String options:

Option | Description
---- | ----
brokerlist | List of one or more broker addresses.
maxprefetch | The maximum number of pre-fetched messages per Session. If not specified, default value of 500 is used.
sync_publish | If the value is `all` the client library waits for confirmation when sending a message.
sync_ack | A sync command is sent after every acknowledgement to guarantee that it has been received.
failover | One of: `singlebroker`, `roundrobin`, `nofailover`. This option controls failover behavior. The method `singlebroker` uses only the first broker in the list, `roundrobin` will try each broker given in the broker list until a connection is established, `nofailover` disables all retry and failover logic. The broker list options `retries` and `connectdelay` determine the number of times a connection to a broker will be retried and the length of time to wait between successive connection attempts before moving on to the next broker in the list. The failover option `cyclecount` controls the number of times to loop through the list of available brokers before finally giving up. Defaults to `roundrobin` if the brokerlist contains multiple brokers, or `singlebroker` otherwise.
ssl | If `true`, use SSL for all broker connections. Overrides any per-broker settings in the brokerlist (see below) entries. If not specified, the brokerlist entry for each given broker is used to determine whether SSL is used.

For example, the following connection string specifies a user name, a password, a client ID, a virtual host ("test"), a broker list with a single broker: a TCP host with the host name “localhost” using port 5672:

```
amqp://username:password@clientid/test?brokerlist='tcp://localhost:5672'
```

For full documentation about AMQP URL format, see [AMQP Connection URL Protocols](https://qpid.apache.org/releases/qpid-0.32/jms-client-0-8/book/JMS-Client-0-8-Connection-URL.html).

### Universal JMS access
You can register any JMS connectionFactory in the Apache Karaf container using [this guide](https://karaf.apache.org/manual/latest/users-guide/jms.html). After these manipulations you can use JMS connection via JNDI. Use the following connection string format for using JNDI-based JMS access:

```
jndi://<path-to-connection-factory>
```

For example, the following connection string specifies JMS connectionFactory located at `/jms/test`:

```
jndi:///jms/test  
```

## Configuration Parameters
MQ Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
expirationTime | Integer | No | Expiration time (in millis) of attribute values in the storage. If attribute was not updated by external component then Resource Adapter can't obtain its value. This is very helpful for detecting availability of the component. Default is infinite (never expires) | `2000`
userName | String | No | Specifies user name used for authentication in Messaging Middleware. It is recommended to use connection string instead of explicitly definition of credentials via configuration parameters. | `guest`
password | String | No | Specifies password used for authentication in Messaging Middleware. It is recommended to use connection string instead of explicitly definition of credentials via configuration parameters. | `qwerty`
inputQueueName | String | Yes | Name of the queue to listen. Each message in this queue will be consumed by MQ Connector and interpreted as new attribute value or notification. For AMQP-specific MQ this parameter has advanced format (see below) | `deadLetterQueue`
isInputTopic | Boolean | No | `true` to use topic instead of queue for messages to listen. Default is `false` | `true`
outputQueueName | String | No | Name of the queue to deliver attribute value when it changed by resource adapter. | `changedAttributes`
isOutputTopic | Boolean | No | `true` to use topic instead of queue for messages to produce. Default is `false` | `true`
converterScript | String | No | Absolute file path to Groovy script file which contains custom JMS message conversion logic | `/etc/snamp/mq/converter.groovy`
amqpVersion | Enum: `0-8`, `0-9`, `0-9-1`, `0-10` | No | Version of AMQP protocol specification. This parameter has significance only for `amqp:` connection string. Default is `0-9-1` (for RabbitMQ interoperability) | `0-10`

### AMQP queue names
This section explains how to configure `inputQueueName` and `outputQueueName` for AMQP protocol.

AMQP protocol introduces additional entities such as `Exchange` and `Routing Key`. JMS doesn't support these entities natively. So you must specify queue name in the form of [Binding URL](https://qpid.apache.org/releases/qpid-0.32/jms-client-0-8/book/JMS-Client-0-8-Binding-URL.html):

```
BURL:<Exchange Class>://<Exchange Name>/[<Destination>]/[<Queue>][?<option>='<value>'[&<option>='<value>']]
```
where `Exchange Class`, specifies the type of the exchange, for example, _direct_, _topic_, _fanout_, etc. `Exchange Name`, specifies the name of the exchange, for example, _amq.direct_, _amq.topic_, etc. `Destination`, is an optional part of Binding URL. It can be used to specify a **routing key** with the non direct exchanges if an option `routingkey` is not specified. If both Destination and option `routingkey` are specified, then option `routingkey` has precedence. `Queue`, is an optional part of Binding URL to specify a queue name for JMS queue destination. It is ignored in JMS topic destinations. Queue names may consist of any mixture of digits, letters, and underscores. `Options`, key-value pairs separated by '=' character specifying queue and exchange creation arguments, routing key, client behavior:

Option | Description
---- | ----
durable | Queue durability flag. If it is set to true, a durable queue is requested to create. The durable queue should be stored on the Broker and remained there after Broker restarts until it is explicitly deleted. This option has no meaning for JMS topic destinations, as by nature a topic destination only exists when a subscriber is connected. If durability is required for topic destinations, the durable subscription should be created.
exclusive | Queue exclusivity flag. The client cannot use a queue that was declared as exclusive by another still-open connection.
autodelete | Queue auto-deletion flag. If it is set to true on queue creation, the queue is deleted if there are no remaining subscribers.
exchangeautodelete | Exchange auto-deletion flag.
exchangedurable | Exchange durability flag. If it is set to true when creating a new exchange, the exchange will be marked as durable. Durable exchanges should remain active after Broker restarts. Non-durable exchanges are deleted on following Broker restart.
routingkey | Defines the value of the binding key to bind a queue to the exchange. It is always required to specify for JMS topic destinations. If routing key option is not set in Binding URL and direct exchange class is specified, the queue name is used as a routing key. MQ Connector uses routing key to publish messages onto exchange.
browse | If set to true on a destination for a message consumer, such consumer can only read messages on the queue but cannot consume them. The consumer behaves like a queue browser in this case.

Example of Binding URL used as queue name:

```
BURL:direct://amq.direct//snampQueue?durable='true'
```

## Configuring attributes
Each attribute configured in MDA Resource Connector has the following configuration schema:

* `Name` - storage key used to write attribute value into the internal storage
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
expectedType | Enum | Yes | Type of the attribute | `bool`
dictionaryItemNames | Comma-separated list of strings | If **expectedType**=`dictionary` | Comma-separated list of dictionary fields | `total, used`
dictionaryItemTypes | Comma-separated list of enum values |  If **expectedType**=`dictionary` | Comma-separated list of types for each field if **expectedType** is `dictionary` | `int32, int64`
dictionaryName | String |  If **expectedType**=`dictionary` | The name of dictionary type | `MemoryStatus`

Note that parameters related to thread pool are omitted. See **SNAMP Configuration Guide** page for more information about thread pool configuration. All other parameters will be ignored.

### Supported types
**expectedType** configuration parameter must have one of the following values:

Value | Description
---- | ----
int8 | 8-bit integer
int16 | 16-bit integer
int32 | 32-bit integer
int64 | 64-bit integer
float32 | Floating-point number with single precision
float64 | Floating-point number with double precision
bool | boolean flag (`true`/`false`)
string | Text
datetime | Date/time value (usually used as timestamp)
objectname | JMX Object Name
bigint | Integer with arbitrary precision
bigdecimal | Real number with arbitrary precision
char | Single character
array(int8) | Array of 8-bit integers
array(int16) | Array of 16-bit integers
array(int32) | Array of 32-bit integers
array(int64) | Array of 64-bit integers
array(bool) | Array of flags
array(string) | Array of strings
array(char) | Array of characters (it is highly recommended to use `string` instead)
array(float32) | Array of floating-points numbers with single precision
array(float64) | Array of floating-points numbers with double precision
array(objectname) | Array of JMX Object Names
array(datetime) | Array of timestamps
array(bigint) | Array of arbitrary integers
array(bigdecimal) | Array of arbitrary reals
dictionary | Composite structure consisting of several fields
table | A set of rows

## Configuring events
Each event configured in MDA Resource Connector has the following configuration schema:

* `Category` - category name used to catch messages from the input queue
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
expectedType | Enum | Yes | Type of the attribute | `bool`
dictionaryItemNames | Comma-separated list of strings | If **expectedType**=`dictionary` | Comma-separated list of dictionary fields | `total, used`
dictionaryItemTypes | Comma-separated list of enum values |  If **expectedType**=`dictionary` | Comma-separated list of types for each field if **expectedType** is `dictionary` | `int32, int64`
dictionaryName | String |  If **expectedType**=`dictionary` | The name of dictionary type | `MemoryStatus`

## Message Format
MQ Connector interprets the following types of messages:
* `write` message consumed from the input queue that carries a new attribute value
* `notify` message consumed from the input queue that carries a new notification
* `attributeChanged` message produced to the output queue (if configured) when attribute value was changed by any resource adapter

Message type is stored as [JMSType](https://docs.oracle.com/javaee/7/api/javax/jms/Message.html#setJMSType-java.lang.String-) property of JMS message. The following table describes relationship between JMS header and message type :

Message Type | JMS Header | Description
---- | ---- | ----
write, attributeChanged | snampStorageKey | Storage key used as a name of the attribute
notify | snampMessage | Human-readable text associated with the notification
notify | snampCategory | Category of the event to deliver into MQ Connector
notify | snampSequenceNumber | Sequence number of the notification

See [Java JMS Mapping to AMQP Message Properties](https://qpid.apache.org/releases/qpid-0.32/programming/book/ch03s03.html) for information about mapping of JMS headers to AMQP protocol.

Interpretation of JMS message body depends on the message type:

Message Type | Message Body
---- | ----
write, attributeChanged | Value of the attribute. Cannot be empty.
notify | Notification payload. May be empty.

Serialization formats for each data type:

SNAMP Data Type | [BytesMessage](https://docs.oracle.com/javaee/7/api/javax/jms/BytesMessage.html) | [TextMessage](https://docs.oracle.com/javaee/7/api/javax/jms/TextMessage.html) | [StreamMessage](https://docs.oracle.com/javaee/7/api/javax/jms/StreamMessage.html)
---- | ----
int8 | Single byte | Number as string | Byte
int16 | 2 bytes in big-endian format | Number as string | Short
int32 | 4 bytes in big-endian format | Number as string | Integer
int64 | 8 bytes in big-endian format | Number as string | Long
bool | Single byte | `true` or `false` | Boolean
string | UTF string | Plain text | String
float32 | 4 bytes in big-endian format | Real number as string | Float
float64 | 8 bytes in big-endian format | Real number as string | Double
objectname | UTF string | Plain text | String
datetime | 8 bytes in big-endian format (Unix time) | Number as string | Long
char | 2 bytes in big-endian format | String with a single character | Char
bigint | UTF string | Plain text | _Not supported_
bigdecimal | UTF string | Plain text | _Not supported_
array(int8) | Raw bytes | _Not supported_ | _Not supported_
array(char) | UTF string | Plain text | String
array(int16) | Array of bytes in big-endian format | _Not supported_ | _Not supported_
array(int32) | Array of bytes in big-endian format | _Not supported_ | _Not supported_
array(int64) | Array of bytes in big-endian format | _Not supported_ | _Not supported_
array(float32) | Array of bytes in big-endian format | _Not supported_ | _Not supported_
array(float64) | Array of bytes in big-endian format | _Not supported_ | _Not supported_
array(string) | _Not supported_ | _Not supported_ | _Not supported_
array(objectname) | _Not supported_ | _Not supported_ | _Not supported_
array(bigint) | _Not supported_ | _Not supported_ | _Not supported_
array(bigdecimal) | _Not supported_ | _Not supported_ | _Not supported_
array(datetime) | Array of bytes in big-endian format | _Not supported_ | _Not supported_
array(bool) | Array of bytes representing bit mask | _Not supported_ | _Not supported_

`dictionary` data type can be mapped as [MapMessage](https://docs.oracle.com/javaee/7/api/javax/jms/MapMessage.html).

Also it is possible to deliver any serialized Java object using [ObjectMessage](https://docs.oracle.com/javaee/7/api/javax/jms/ObjectMessage.html). This is helpful when standard mapping described above is not applicable (including `table` data type).

## Custom Converter
Mapping of data types and message headers can be individually overridden by custom Groovy script. You can override one or more conversion methods using power of Groovy language including Grape dependencies.

The following table contains signature of all possible conversion methods:

Method | Description
---- | ----
com.bytex.snamp.connectors.mq.jms.SnampMessageType getMessageType(javax.jms.Message message) | Detects message type
void getMessageType(javax.jms.Message message, com.bytex.snamp.connectors.mq.jms.SnampMessageType type) | Stores message type into the specified message
String getStorageKey(javax.jms.Message message) | Extracts storage key from the message
void setStorageKey(javax.jms.Message message, String storageKey) | Saves storage key to the message
String getMessage(javax.jms.Message message) | Extracts notification message from JMS message
String getCategory(javax.jms.Message message) | Extracts notification category from JMS message
long getSequenceNumber(javax.jms.Message message) | Extracts message sequence number
byte toByte(javax.jms.Message msg) | Converts MQ message to single byte
short toShort(javax.jms.Message msg) | Converts MQ message to 2-byte signed integer
int toInteger(javax.jms.Message msg) | Converts MQ message to 4-byte signed integer
long toLong(javax.jms.Message msg) | Converts MQ message to 8-byte signed integer
String toString(javax.jms.Message msg) | Converts MQ message to Unicode string
char toChar(javax.jms.Message msg) | Converts MQ message to Unicode character
java.util.Date toDate(javax.jms.Message msg) | Converts MQ message to time stamp
BigInteger toBigInt(javax.jms.Message msg) | Converts MQ message to the integer with cardinal precision
String toBigDecimal(javax.jms.Message msg) | Converts MQ message to the real number with cardinal precision
javax.management.ObjectName toObjectName(javax.jms.Message msg) | Converts MQ message to JMX Object Name
boolean toBoolean(javax.jms.Message msg) | Converts MQ message to boolean flag
float toFloat(javax.jms.Message msg) | Converts MQ message to floating-point number with single precision
double toDouble(javax.jms.Message msg) | Converts MQ message to floating-point number with double precision
byte[] toByteArray(javax.jms.Message msg) | Converts MQ message to array of bytes
Byte[] toWrappedByteArray(javax.jms.Message msg) | Converts MQ message to wrapped array of 8-bit signed integers
short[] toShortArray(javax.jms.Message msg) | Converts MQ message to array of 16-bit signed integers
Short[] toWrappedShortArray(javax.jms.Message msg) | Converts MQ message to wrapped array of 16-bit signed integers
int[] toIntArray(javax.jms.Message msg) | Converts MQ message to array of 32-bit signed integers
Integer[] toWrappedIntArray(javax.jms.Message msg) | Converts MQ message to array of 32-bit signed integers
long[] toLongArray(javax.jms.Message msg) | Converts MQ message to array of 64-bit signed integers
Long[] toWrappedLongArray(javax.jms.Message msg) | Converts MQ message to array of 64-bit signed integers
char[] toCharArray(javax.jms.Message msg) | Converts MQ message to array of Unicode characters
Character[] toWrappedCharArray(javax.jms.Message msg) | Converts MQ message to array of Unicode characters
float[] toFloatArray(javax.jms.Message msg) | Converts MQ message to array of floating-point numbers with single precision
Float[] toWrappedFloatArray(javax.jms.Message msg) | Converts MQ message to array of floating-point numbers with single precision
double[] toDoubleArray(javax.jms.Message msg) | Converts MQ message to array of floating-point numbers with double precision
Double[] toWrappedDoubleArray(javax.jms.Message msg) | Converts MQ message to array of floating-point numbers with double precision
boolean[] toBoolArray(javax.jms.Message msg) | Converts MQ message to array of boolean flags
Boolean[] toWrappedBoolArray(javax.jms.Message msg) | Converts MQ message to array of boolean flags
java.util.Date[] toDateArray(javax.jms.Message msg) | Converts MQ message to array of time stamps
CompositeData toCompositeData(javax.jms.Message msg, CompositeType type) | Converts MQ message to the dictionary
javax.jms.Message fromByte(byte value, javax.jms.Session session) | Converts 8-bit signed integer into JMS message
javax.jms.Message fromShort(short value, javax.jms.Session session) | Converts 16-bit signed integer into JMS message
javax.jms.Message fromInt(byte value, javax.jms.Session session) | Converts 32-bit signed integer into JMS message
javax.jms.Message fromLong(byte value, javax.jms.Session session) | Converts 64-bit signed integer into JMS message
javax.jms.Message fromBoolean(boolean value, javax.jms.Session session) | Converts boolean flag into JMS message
javax.jms.Message fromFloat(float value, javax.jms.Session session) | Converts floating-point number into JMS message
javax.jms.Message fromDouble(double value, javax.jms.Session session) | Converts floating-point number into JMS message
javax.jms.Message fromChar(char value, javax.jms.Session session) | Converts single Unicode character into JMS message
javax.jms.Message fromChar(String value, javax.jms.Session session) | Converts Unicode string into JMS message
javax.jms.Message fromBigInt(BigInteger value, javax.jms.Session session) | Converts integer with unlimited precision into JMS message
javax.jms.Message fromBigDecimal(BigDecimal value, javax.jms.Session session) | Converts floating-point number with unlimited precision into JMS message
javax.jms.Message fromObjectName(ObjectName value, javax.jms.Session session) | Converts JMX Object Name into JMS message
javax.jms.Message fromDate(Date value, javax.jms.Session session) | Converts time stamp into JMS message
javax.jms.Message fromCompositeData(CompositeData value, javax.jms.Session session) | Converts dictionary into JMS message
javax.jms.Message fromByteArray(byte[] value, javax.jms.Session session) | Converts array of bytes into JMS message
javax.jms.Message fromShortArray(short[] value, javax.jms.Session session) | Converts array of 16-bit signed integers into JMS message
javax.jms.Message fromShortArray(Short[] value, javax.jms.Session session) | Converts array of 16-bit signed integers into JMS message
javax.jms.Message fromIntArray(int[] value, javax.jms.Session session) | Converts array of 32-bit signed integers into JMS message
javax.jms.Message fromIntArray(Integer[] value, javax.jms.Session session) | Converts array of 32-bit signed integers into JMS message
javax.jms.Message fromLongArray(long[] value, javax.jms.Session session) | Converts array of 64-bit signed integers into JMS message
javax.jms.Message fromLongArray(Long[] value, javax.jms.Session session) | Converts array of 64-bit signed integers into JMS message
javax.jms.Message fromFloatArray(float[] value, javax.jms.Session session) | Converts array of floating-point numbers into JMS message
javax.jms.Message fromFloatArray(Float[] value, javax.jms.Session session) | Converts array of floating-point numbers into JMS message
javax.jms.Message fromDoubleArray(double[] value, javax.jms.Session session) | Converts array of floating-point numbers into JMS message
javax.jms.Message fromDoubleArray(Double[] value, javax.jms.Session session) | Converts array of floating-point numbers into JMS message
javax.jms.Message fromCharArray(char[] value, javax.jms.Session session) | Converts array of Unicode characters into JMS message
javax.jms.Message fromCharArray(Character[] value, javax.jms.Session session) | Converts array of Unicode characters into JMS message
javax.jms.Message fromDateArray(Date[] value, javax.jms.Session session) | Converts array of time stamps into JMS message

The following example demonstrates how override **String** conversion:

```groovy
import javax.jms.Message

println "Script loaded"

String toString(Message message){
    return message.getText()
}
```

It is not possible to override conversion of [ObjectMessage](https://docs.oracle.com/javaee/7/api/javax/jms/ObjectMessage.html).
