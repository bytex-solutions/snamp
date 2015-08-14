IBM WebSphere MQ Connector
====
WMQ Resource Connector allows to monitor WebSphere MQ queues using Queue Manager. This connector provides set of predefined attributes. Events are not supported.

The named queue (or topic) is interpreted as Managed Resource by this connector.

## Initial setup
SNAMP distribution package doesn't contain IBM WebSphere MQ classes for Java due to license restrictions. These classes should be installed into OSGi environment manually:
1. Verify IBM WebSphere MQ server installation
1. Install IBM WebSphere MQ client on the same machine where SNAMP installed. See [Installing a WebSphere MQ client](http://www-01.ibm.com/support/knowledgecenter/SSFKSJ_7.5.0/com.ibm.mq.ins.doc/q008960_.htm) for more details.
1. Copy JARs from `<MQ_INSTALLATION_PATH>/java/lib/OSGi` into `<snamp>/deploy` folder
1. Set your system path or library path correctly so that the OSGi runtime environment can find any required DLL files or shared libraries. See [Support for OSGi on WebSphere MQ classes for Java](http://www-01.ibm.com/support/knowledgecenter/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q030630_.htm) for more details.
1. Restart SNAMP if necessary

Verify correctness of installation:
1. With `bundle:list` command in the shell console. The output must contain following strings (version may depend on the IBM WebSphere MQ version):
```
117 | Active    |  80 | 7.5.0.2                    | WMQ prereq Plug-in
146 | Active    |  80 | 7.5.0.2                    | WebSphere MQ classes for Java Plug-in
147 | Active    |  80 | 7.5.0.2                    | Common Services J2SE Plug-in
```
1. With `log:exception-display`. Following message should not be presented in the log:
```
com.bytex.snamp.connectors.wmq.MQConnectorActivator$WMQJavaClassesNotInstalled: WebSphere MQ classes for Java are not installed into OSGi environment
```

## Connection String
Connection string should be specified in URL format with `wsmq` schema:
```
wsmq://<username>:<password>@<mq-server-host>:<port>/<channelName>/<queueName>
```
WMQ Connector interacts with queue identified by _channelName_ and _queueName_ parameters. It is recommended to verify your connection string with `mqivp` tool. See [Verifying your WebSphere MQ classes for Java installation with the sample application](http://www-01.ibm.com/support/knowledgecenter/SSFKSJ_7.5.0/com.ibm.mq.dev.doc/q030690_.htm) for more details.

Examples:
* `wsmq://root:qwerty@192.168.0.1:1414/channel/topic`
* `wsmq://192.168.0.1:1414/channel/topic2` for insecure connection

## Configuration Parameters
WMQ Resource Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
queueManager | String | No | Name of the Queue Manager | `mq1`
smartMode | Boolean | No | Enables or disables smart mode | `true`

## Configuring attributes
Each attribute configured in WMQ Resource Connector has the following configuration schema:
* `Name` - name of predefined attribute. Following names are supported (attribute types described in SNAMP **Management Information Model**):

Name | Type | Meaning
---- | ---- | ----
active | bool | Indicates that queue/topic is available. Useful for health check
lastGetDate | date | Time at which the last message was destructively read from the queue
lastPutDate | date | Time at which the last message was successfully put to the queue
openHandlesForInput | int32 | Number of handles that are currently valid for removing messages from the queue by means of the MQGET call
openHandlesForOutput | int32 | Number of handles that are currently valid for adding messages to the queue by means of the MQPUT call
queueDepth | int32 | Number of messages currently in the queue
oldestMessageAge | int32 | Age, in seconds, of the oldest message in the queue
messageOnQueueTime | int32 | Amount of time, in microseconds, that a message spent in the queue
uncommittedMessagesCount | int32 | Number of uncommitted changes (puts and gets) pending for the queue
totalBytesSent | int64 | Number of bytes sent through the channel
bytesSentLastHour | int64 | Number of bytes sent through the channel for the last hour
bytesSentLast24Hours | int64 | Number of bytes sent through the channel for the last day
totalBytesReceived | int64 | Number of bytes received through the channel
bytesReceivedLastHour | int64 | Number of bytes received through the channel for the last hour
bytesReceivedLast24Hours | int64 | Number of bytes received through the channel for the last day
processedMessagesCount | int32 | Number of messages sent or received, or number of MQI calls handled
processedMessagesCountLastHour | int32 | Number of messages sent or received, or number of MQI calls handled for the last hour
processedMessagesCountLast24Hours | int32 | Number of messages sent or received, or number of MQI calls handled for the last day

* Configuration parameters are not supported. Any user-defined configuration parameter will be ignored.

## Smart mode
WMQ Resource Connector provides support of Smart mode. Therefore the connector can automatically expose attributes without manual configuration. The attributes described above will be automatically exposed to adapters. System name of the attribute will be used as user-defined name.
