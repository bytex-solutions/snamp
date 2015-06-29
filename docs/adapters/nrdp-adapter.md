NRDP Resource Adapter
====
NRDP Resource Adapter allows to collect monitoring and management information from all resources connected to SNAMP using [passive check](http://nagios.sourceforge.net/docs/3_0/passivechecks.html). It utilizes [Nagiod Remote Data Processing](https://assets.nagios.com/downloads/nrdp/docs/NRDP_Overview.pdf) technology. So, you need to configure NRDP agent to receive information in XML format from SNAMP.

The resource adapter sends check information about connected resources in XML to NRDP agent at the specified period of time.

Nagios Resource Adapter supports the following features (if they are supported by managed resources too):

Feature | Description
---- | ----
Attributes | Each attribute will be transferred to NRDP agent at the specified period of time
Notifications | Each notification will be delivered asynchronously to NRDP agent

Note that this adapter utilizes **its own internal thread pool that can be configured explicitly**.

For more information about NRDP and its XML format see [NRDP Overview](https://assets.nagios.com/downloads/nrdp/docs/NRDP_Overview.pdf).

## Configuration Parameters
NRDP Resource Adapters recognizes the following configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
serverURL | URL | Yes | An address for NRDP remote server | `http://nagios.mydomain.com/nrdp`
connectionTimeout | Integer | No | HTTP connection timeout (in millis) used by SNAMP when connecting to NRDP server. By default it is equal to 4 seconds | `6000`
token | String | Yes | Authentication token configured in NRDP server and required for authentication of passive check senders | `xyzterw`
passiveCheckSendPeriod | Integer | No | The period of passive check sent to NRDP server by the resource adapter. This parameter affects only attributes because notifications will be delivered asynchronously. By default it is equal to 1 second | `2000`

Note that parameters related to thread pool is omitted. See **SNAMP Configuration Guide** page for more information about thread pool configuration. All other parameters will be ignored.

## Configuring attributes
The following configuration parameters of the attributes have influence on NRDP Resource Adapter behavior:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
serviceName | String | No | The service name that will be specified in the passive check packet. If it is not specified then user-defined name of the connected resource will be used instead. The service name helps to specify more informative name of the monitored resource in Nagios. | `internet-bank-cluster-node-0`
minValue | String | No | The minimum possible value (exclusive) of the attribute | `3000`
maxValue | String | No | The maximum possible value (exclusive) of the attribute | `10000`
units | String | No | Unit of measurement (UOM) of the attribute value. For example: `ms`, `m`, `kg`, `MB` | `MB`

NRDP Resource Adapter automatically detects the service status using `minValue` and `maxValue` if it is specified in the configuration. `OK` status will be assigned using the following rule: `minValue < actual < maxValue`.

Additionally, if read operation fails with exception then the adapter informs about `CRITICAL` status of the service.

## Configuring events
The following configuration parameters of the events have influence on NRDP Resource Adapter behavior:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
serviceName | String | No | The service name that will be specified in the passive check packet. If it is not specified then user-defined name of the connected resource will be used instead. The service name helps to specify more informative name of the monitored resource in Nagios. | `internet-bank-cluster-node-0`
