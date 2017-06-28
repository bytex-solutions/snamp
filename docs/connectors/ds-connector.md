Data Stream Connectors
====
This is a family of passive Resource Connectors used for processing stream of input data provided by **managed resources**. The stream is converted into rich set of metrics exposed as attributes to SNAMP Web Console and Gateways. Resource Connectors in this family may use any transport to receive measurements from **managed resources** like HTTP, event queues or stream processing platforms like Apache Kafka. By default, model of input measurements based on **SNAMP Instrumentation Library** that describes protocol and language bindings. This approach is very similar to [Apache HTrace](http://htrace.incubator.apache.org/).

Data Stream Connectors allow to process the following information:
* Instant measurements of numeric, boolean and textual metrics
* Timing of actions (start time, stop time and duration)
* Spans represent tracing information that describe block of execution such as communication with remote microservices
* Health checks

Metrics collected from received measurements are exposed as attributes by all Resource Connectors in the family.

## Configuring connectors
All connectors in the family shares the same configuration properties:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
synchronizationPeriod | int64 | No | Synchronization period (in millis) used by SNAMP to synchronize metrics across SNAMP cluster. Default is 5000 | `2000`
heartbeat | int64 | No | Allowed time span (in millis) between two incoming health checks. If it is timed out then resource connector reports bad health status. If this parameter is not specified then health check will be disabled | `3000`

Other configuration parameters depends on type of derived Resource Connector.

## Configuring attributes
All connectors in the family supports the same basic set of attributes and its configuration parameters:
* _Name_ - name of attribute. Can be selected from predefined set of names.
* Configuration parameters:

> Information in this section heavily relies on information about Gauges from **SNAMP Management Information Model**

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
from | int64 or float64 | Yes for ranged gauges | The value indicating start of the confidential interval | `50`
to | int64 or float64 | Yes for ranged gauges | The value indicating end of the confidential interval | `100`
filter | string | No | Groovy script used to filter input measurements to be processed by attribute | `return type = 'MySQL'`
channels | int64 | No | Number of channels used to compute _ArrivalsGauge_. Default is 1 | `2`
gauge | String | Yes | Type of gauge to be provided by attribute | `gauge64`

### Groovy-based filtering
Groovy-based filter is just a block of Groovy code used to accept or ignore input measurement to be processed by attributes or notifications. Script should return `true` to accept measurement and `false` to ignore it. Script has full access to the measurement through global script properties.

### Possible values of parameter _gauge_
This table describes relationship between value of parameter _gauge_, gauges described in **SNAMP Management Information Model** and type of measurements used to compute these gauges:

Value | Gauge type | Used measurements | Comments
---- | ---- | ----
gauge64 | RatedGauge64 | Instant measurement of integer values |
gaugeFP | RatedGaugeFP | Instant measurement of floating-point and integer values |
flag | RatedFlagGauge | Instant measurement of boolean values (flags) |
stringGauge | StringGauge | Instant measurement of text values |
timer | RatedTimerGauge | Timing of actions and spans |
rangedGauge64 | RangedGauge64 | Instant measurement of integer values | Configuration parameters `from` and `to` are required
rangedGaugeFP | RangedGaugeFP | Instant measurement of floating-point and integer values | Configuration parameters `from` and `to` are required
arrivals | ArrivalsGauge | Timing of actions and spans | Configuration parameter `channels` can be used to specify number of channels allowed to process requests in parallel
notificationRate | RateGauge | Rate of notification which category is specified in configuration parameter `name` |

## Configuring events
All connectors in the family supports the same basic set of notifications and its configuration parameters:
* _Category_ - category of notifications. Can be selected from predefined set of categories.
* Configuration parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
filter | string | No | Groovy script used to filter notifications | `return type = 'MySQL'`

### Predefined categories
Category | Description
---- | ----
jmx.attribute.change | Notification about changing of the attribute exposed by connector
com.bytex.snamp.measurement.stopwatch | Notification about receiving of time measurement
com.bytex.snamp.measurement.span | Notification about receiving of span
com.bytex.snamp.measurement.value | Notification about receiving of instant measurement

## Configuring operations
All connectors in the family supports the same basic set of notifications and its configuration parameters:
* _Name_ - name of operation. Can be selected from predefined set of categories.
* Configuration parameters are not used by predefined set of operations

A set of operations can be extended by derived Resource Connectors.

### Predefined operations
A set of predefined operations:

Operation name | Parameters | Result | Description
---- | ---- | ---- | ----
resetAllMetrics | _No parameters_ | _No result_ | Resets all gauges to its initial values
resetMetric | attributeName(string) | _No result_ | Resets gauge provided by attribute

## Health check
**Managed resource** should send health check periodically. Health check is valid during period of time specified in `heartbeat` parameter.
