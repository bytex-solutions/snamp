SNAMP Management Information Model
====
SNAMP Management Information Model is a special abstraction layer specifying data exchange between **Resource Connector** and **Gateway**. This layer hides the details of the backed management protocol.

![Management Information Flow](images/inform-flow.png)

As far as you can see at the figure above, **Resource Connector** converts protocol-specific data to the entities in **Management Information Model**.
**Gateway** uses this unified representation of the management information and transforms it to another management protocol (expected by **monitoring & management tool**).

Management Information Model consists of the following entities:

* Attributes
* Events
* Operations
* Health checks

The attribute has its _data type_ that reflects format (or semantics) of attribute value.

The event emitted by **managed resource** will be translated into the unified object and delivered to **Resource Adapter** via **Resource Connector**.

The operation has its own set of _formal parameters_ and _return value_. Each parameter and return value belong to some _data type_.

## Type System
SNAMP supports the following set of data types:

* Scalar types
  * datetime - number of milliseconds since January 1, 1970, 00:00:00 GMT
  * int8 - 8-bit signed integer
  * int16 - 16-bit signed integer
  * int32 - 32-bit signed integer
  * int64 - 64-bit signed integer
  * float32 - IEEE-754 floating-point number with single precision
  * float64 - IEEE-754 floating-point number with double precision
  * bigint - integer with arbitrary precision
  * bigdecimal - floating-point number with arbitrary precision
  * char - 16-bit Unicode single character
  * string - 16-bit Unicode string
  * objectname - [JMX Object Identifier](http://www.oracle.com/technetwork/java/javase/tech/best-practices-jsp-136021.html#mozTocId509360)
  * bool - boolean data type with two possible values: `true` and `false`
* Array data types
  * array(datetime) - array of date/time values
  * array(int8) - array of bytes
  * array(int16) - array of 16-bit words
  * array(int32) - array of 32-bit words
  * array(int64) - array of 64-bit words
  * array(float32) - array of numbers with single precision
  * array(float64) - array of numbers with double precision
  * array(bigint) - array of integers with arbitrary precision
  * array(bigdecimal) - array of real numbers with arbitrary precision
  * array(char) - array of Unicode characters
  * array(string) - array of strings
  * array(objectname) - array of object names
  * array(bool) - array of boolean values
* Dictionary - set of key/value pairs with unique keys. Each key is a _string_. The value in the pair may belong to any scalar type supported by SNAMP Management Information Model
* Table - set of rows and columns. The name of the column is a _string_. The cell value may belong to any scalar type supported by SNAMP Management Information Model

You should take into account that the semantics of the protocol-specific data type might be lost during conversion. For example, we have configured SNMP Adapter and JMX Connector. JMX-compliant managed resource exposes attribute of `Float` JMX type. This JMX-specific type can be converted directly to `float` value in SNAMP Management Information Model. But ASN.1 type system (used by SNMP protocol) doesn't have support for IEEE-754 floating-point numbers. Therefore, SNMP Adapter converts `float` value to ASN.1 `OCTET_STRING`.  
> Some Resource Adapter provide configuration properties allowing you to specify conversion rules. For example, `int32` value may be converted into ASN.1 `INTEGER_32` or 4-byte `OCTET_STRING`.

## Notification
Notification is a message emitted by connected **managed resource** and routed to **Resource Adapter** through **Resource Connector**. SNAMP Management Information Model defines unified way for representing notifications called **Notification Object**.

The structure of **Notification Object**:

Field | Data Type | Description
---- | ---- | ----
TimeStamp | datetime | Notification emission date
Source | string | Name of the managed resource emitting notification
Type | string | Name of the notification (differs from event category)
Message | string | Human-readable description of the notification
Sequence Number | int64 | Notification sequence number within the source. That is the serial number identifying particular instance of notification (in the context of the notification source). Notification model does not assume that notifications will be received in the same order that they are sent. Sequence number helps you to sort received notifications
Payload | _any supported data type_ | Additional payload delivered from **managed resource**. Semantics of this part of the notification depends on the connected **managed reresource** and its management protocol

## Health status
Health status information can be provided by supervisor or resource connector.

### Resource health status
Health status provided by resource connector is called **resource health status** and has the following types:
* **OK** indicates that everything if fine. It has the following characteristics:

Characteristic | Description
---- | ----
Timestamp | Contains time stamp of the health check

* **Malfunction** indicates that **managed resource** is fully or partially unavailable. It has the following characteristics:

Characteristic | Description
---- | ----
Timestamp | Contains time stamp of the health check
Malfunction level | Describes level of malfunction
Data | Optional set of key/value pairs with additional important information about malfunction

Possible malfunction levels:
* _Low_ - warning messages, not an error, but indication that an error will occur if action is not taken, e.g. file system 85% full - each item must be resolved within given time.
* _Moderate_ - non-urgent failures, these should be relayed to developers or admins; each item must be resolved within a given time.
* _Substantial_ - should be corrected immediately, but indicates failure in a secondary system, an example is a loss of a backup ISP connection.
* _Severe_ - should be corrected immediately, therefore notify staff who can fix the problem, an example would be the loss of a primary ISP connection.
* _Critical_ - a "panic" condition usually affecting multiple apps/servers/sites. At this level it would usually notify all tech staff on call.

### Group health status
Group health status is provided by supervisor and consists of summary health status of the group and list of health statuses for each resource in the group. Summary health status may reflection additional information like cluster malfunction. Structure of the summary health status is identical to health status of the resource.

## Gauges
Some resource connectors like _composite_ or _zipkin_ connector may expose gauges with statistics information. These gauges divided into the following categories:

* **RangedGauge** measures location of some value in the user-defined range (for example, [0..10]):

Field | Data type | Description
---- | ---- | ----
lessThanRange | float64 | Percents of values that are less than lower bound of the range
lessThanRangeLastSecond | float64 | Percents of values that are less than lower bound of the range for the last second
lessThanRangeLastMinute | float64 | Percents of values that are less than lower bound of the range for the last minute
lessThanRangeLast5Minutes | float64 | Percents of values that are less than lower bound of the range for the last 5 minutes
lessThanRangeLastHour | float64 | Percents of values that are less than lower bound of the range for the last hour
lessThanRangeLast12Hours | float64 | Percents of values that are less than lower bound of the range for the last 12 hours
lessThanRangeLastDay | float64 | Percents of values that are less than lower bound of the range for the last 24 hours
greaterThanRange | float64 | Percents of values that are greater than lower bound of the range
greaterThanRangeLastSecond | float64 | Percents of values that are greater than lower bound of the range for the last second
greaterThanRangeLastMinute | float64 | Percents of values that are greater than lower bound of the range for the last minute
greaterThanRangeLast5Minutes | float64 | Percents of values that are greater than lower bound of the range for the last 5 minutes
greaterThanRangeLastHour | float64 | Percents of values that are greater than lower bound of the range for the last hour
greaterThanRangeLast12Hours | float64 | Percents of values that are greater than lower bound of the range for the last 12 hours
greaterThanRangeLastDay | float64 | Percents of values that are greater than lower bound of the range for the last 24 hours
isInRange | float64 | Percents of values that are in range
isInRangeLastSecond | float64 | Percents of values that are in range for the last second
isInRangeLastMinute | float64 | Percents of values that are in range for the last minute
isInRangeLast5Minutes | float64 | Percents of values that are in range for the last 5 minutes
isInRangeLastHour | float64 | Percents of values that are in range for the last hour
isInRangeLast12Hours | float64 | Percents of values that are in range for the last 12 hours
isInRangeLastDay | float64 | Percents of values that are in range for the last 24 hours

* **Timer** measures timing of actions:

Field | Data type | Description
---- | ---- | ----
summaryValue | float64 | Summary duration of all actions, in seconds
meanValue | float64 | Mean duration of all actions, in seconds
maxValue | float64 | Maximum duration, in seconds
minValue | float64 | Minimum duration, in seconds
lastValue | float64 | Last recorded duration, in seconds
deviation | float64 | Standard deviation of all durations
median | float64 | Percentile 50 of all durations, in seconds
percentile90 | float64 | Percentile 90 of all durations, in seconds
percentile95 | float64 | Percentile 95 of all durations, in seconds
percentile97 | float64 | Percentile 97 of all durations, in seconds
meanValuePerSecond | float64 | Mean value of actions received per second, in seconds
meanValuePerMinute | float64 | Mean value of actions received per minute, in seconds
meanValuePer5Minutes | float64 | Mean value of actions received during 5 minutes, in seconds
meanValuePer15Minutes | float64 | Mean value of actions received during 15 minutes, in seconds
meanValuePerHour | float64 | Mean value of actions received per hour, in seconds
meanValuePer12Hours | float64 | Mean value of actions received during 12 hours, in seconds
meanValuePerDay | float64 | Mean value of actions received during 24 hours, in seconds
maxValueLastSecond | float64 | Maximum duration of the actions for the last second
maxValueLastMinute | float64 | Maximum duration of the actions for the last minute
maxValueLast5Minutes | float64 | Maximum duration of the actions for the last 5 minutes
maxValueLast15Minutes | float64 | Maximum duration of the actions for the last 15 minutes
maxValueLastHour | float64 | Maximum duration of the actions for the last hour
maxValueLast12Hours | float64 | Maximum duration of the actions for the last 12 hours
maxValueLastDay | float64 | Maximum duration of the actions for the last 24 hours
minValueLastSecond | float64 | Minimal duration of the actions for the last second
minValueLastMinute | float64 | Minimal duration of the actions for the last minute
minValueLast5Minutes | float64 | Minimal duration of the actions for the last 5 minutes
minValueLast15Minutes | float64 | Minimal duration of the actions for the last 15 minutes
minValueLastHour | float64 | Minimal duration of the actions for the last hour
minValueLast12Hours | float64 | Minimal duration of the actions for the last 12 hours
minValueLastDay | float64 | Minimal duration of the actions for the last 24 hours
meanTasksPerSecond | float64 | Mean number of actions completed per second
meanTasksPerMinute | float64 | Mean number of actions completed per minute
meanTasksPer5Minutes | float64 | Mean number of actions completed per 5 minutes
meanTasksPer15Minutes | float64 | Mean number of actions completed per 15 minutes
meanTasksPerHour | float64 | Mean number of actions completed per hour
meanTasksPer12Hours | float64 | Mean number of actions completed per 12 hours
meanTasksPerDay | float64 | Mean number of actions completed per day
maxTasksPerSecond | float64 | Maximum number of actions completed per second
maxTasksPertMinute | float64 | Maximum number of actions completed per minute
maxTasksPer5Minutes | float64 | Maximum number of actions completed per 5 minutes
maxTasksPer15Minutes | float64 | Maximum number of actions completed per 15 minutes
maxTasksPerHour | float64 | Maximum number of actions completed per hour
maxTasksPer12Hours | float64 | Maximum number of actions completed per 12 hours
maxTasksPerDay | float64 | Maximum number of actions completed per day
minTasksPerSecond | float64 | Minimal number of actions completed per second
minTasksPerMinute | float64 | Minimal number of actions completed per minute
minTasksPer5Minutes | float64 | Minimal number of actions completed per 5 minutes
minTasksPer15Minutes | float64 | Minimal number of actions completed per 15 minutes
minTasksPerHour | float64 | Minimal number of actions completed per hour
minTasksPer12Hours | float64 | Minimal number of actions completed per 12 hours
minTasksPerDay | float64 | Minimal number of actions completed per day

* **StringGauge** measures `string` values:

Field | Data type | Description
---- | ---- | ----
maxValue | string | Maximum measured value for a whole time
minValue | string | Minimum measured value for a whole time
lastValue | string | Last measured value
maxValueLastSecond | string | Maximum measured value for the last second
maxValueLastMinute | string | Maximum measured value for the last minute
maxValueLast5Minutes | string | Maximum measured value for the last 5 minutes
maxValueLast15Minutes | string | Maximum measured value for the last 15 minutes
maxValueLastHour | string | Maximum measured value for the last hour
maxValueLast12Hours | string | Maximum measured value for the last 12 hours
maxValueLastDay | string | Maximum measured value for the last 24 hours
minValueLastSecond | string | Minimal measured value for the last second
minValueLastMinute | string | Minimal measured value for the last minute
minValueLast5Minutes | string | Minimal measured value for the last 5 minutes
minValueLast15Minutes | string | Minimal measured value for the last 15 minutes
minValueLastHour | string | Minimal measured value for the last hour
minValueLast12Hours | string | Minimal measured value for the last 12 hours
minValueLastDay | string | Minimal measured value for the last 24 hours

* **FlagGauge** measures `boolean` values:

Field | Data type | Description
---- | ---- | ----
lastValue | bool | Last measured value
totalCountOfTrueValues | int64 | Total count of measured `true` values for a whole time
totalCountOfFalseValues | int64 | Total count of measured `false` values for a whole time
totalCountOfTrueValuesLastSecond | int64 | Number of measured `true` values for the last second
totalCountOfTrueValuesLastMinute | int64 | Number of measured `true` values for the last minute
totalCountOfTrueValuesLast5Minutes | int64 | Number of measured `true` values for the last 5 minutes
totalCountOfTrueValuesLast15Minutes | int64 | Number of measured `true` values for the last 15 minutes
totalCountOfTrueValuesLastHour | int64 | Number of measured `true` values for the last hour
totalCountOfTrueValuesLast12Hours | int64 | Number of measured `true` values for the last 12 hours
totalCountOfTrueValuesLastDay | int64 | Number of measured `true` values for the last day
totalCountOfFalseValuesLastSecond | int64 | Number of measured `false` values for the last second
totalCountOfFalseValuesLastMinute | int64 | Number of measured `false` values for the last minute
totalCountOfFalseValuesLast5Minutes | int64 | Number of measured `false` values for the last 5 minutes
totalCountOfFalseValuesLast15Minutes | int64 | Number of measured `false` values for the last 15 minutes
totalCountOfFalseValuesLastHour | int64 | Number of measured `false` values for the last hour
totalCountOfFalseValuesLast12Hours | int64 | Number of measured `false` values for the last 12 hours
totalCountOfFalseValuesLastDay | int64 | Number of measured `false` values for the last day
ratio | float64 | Ratio between number of `true` and `false` values for a whole time
ratioLastSecond | float64 | Ratio between number of `true` and `false` values received for the last second
ratioLastMinute | float64 | Ratio between number of `true` and `false` values received for the last minute
ratioLast5Minutes | float64 | Ratio between number of `true` and `false` values received for the last 5 minutes
ratioLast15Minutes | float64 | Ratio between number of `true` and `false` values received for the last 15 minutes
ratioLastHour | float64 | Ratio between number of `true` and `false` values received for the last hour
ratioLast12Hours | float64 | Ratio between number of `true` and `false` values received for the last 12 hours
ratioLastDay | float64 | Ratio between number of `true` and `false` values received for the last 24 hours

* **RateGauge** measures rate of some events:

Field | Data type | Description
---- | ---- | ----
totalRate | int64 | Total rate of events for a whole time
meanRatePerSecond | float64 | Mean rate of events received per second
meanRatePerMinute | float64 | Mean rate of events received per minute
meanRatePer5Minutes | float64 | Mean rate of events received per 5 minutes
meanRatePer15Minutes | float64 | Mean rate of events received per 15 minutes
meanRatePerHour | float64 | Mean rate of events received per hour
meanRatePer12Hours | float64 | Mean rate of events received per 12 hours
meanRatePerDay | float64 | Mean rate of events received per 24 hours
rateLastSecond | int64 | Rate of events received for the last second
rateLastMinute | int64 | Rate of events received for the last minute
rateLast5Minutes | int64 | Rate of events received for the last 5 minutes
rateLast15Minutes | int64 | Rate of events received for the last 15 minutes
rateLastHour | int64 | Rate of events received for the last hour
rateLast12Hours | int64 | Rate of events received for the last 12 hours
rateLastDay | int64 | Rate of events received for the last 24 hours
maxRatePerSecond | int64 | Max rate per second for a whole time
maxRatePerMinute | int64 | Max rate per minute for a whole time
maxRatePer5Minutes | int64 | Max rate per 5 minutes for a whole time
maxRatePer15Minutes | int64 | Max rate per 15 minutes for a whole time
maxRatePerHour | int64 | Max rate per hour for a whole time
maxRatePer12Hours | int64 | Max rate per 12 hours for a whole time
maxRatePerDay | int64 | Max rate per 24 hours for a whole time
maxRatePerSecondLastMinute | int64 | Max rate per second for the last minute
maxRatePerSecondLast5Minutes | int64 | Max rate per second for the last 5 minutes
maxRatePerSecondLast15Minutes | int64 | Max rate per second for the last 15 minutes
maxRatePerSecondLastHour | int64 | Max rate per second for the last hour
maxRatePerSecondLast12Hours | int64 | Max rate per second for the last 12 hours
maxRatePerSecondLastDay | int64 | Max rate per second for the last 24 hours
maxRatePerMinuteLast5Minutes | int64 | Max rate per minute for the last 5 minutes
maxRatePerMinuteLast15Minutes | int64 | Max rate per minute for the last 15 minutes
maxRatePerMinuteLastHour | int64 | Max rate per minute for the last hour
maxRatePerMinuteLast12Hours | int64 | Max rate per minute for the last 12 hours
maxRatePerMinuteLastDay | int64 | Max rate per minute for the last 24 hours

* **GaugeFP** measures values of type `float64` and `float32`:

Field | Data type | Description
---- | ---- | ----
meanValue | float64 | Mean value for a whole time
maxValue | float64 | Maximum value for a whole time
minValue | float64 | Minimum value for a whole time
lastValue | float64 | Last measured value
deviation | float64 | Standard deviation of all measurements
median | float64 | Percentile 50 of all measurements
percentile90 | float64 | Percentile 90 of all measurements
percentile95 | float64 | Percentile 95 of all measurements
percentile97 | float64 | Percentile 97 of all measurements
meanValuePerSecond | float64 | Mean value per second
meanValuePerMinute | float64 | Mean value per minute
meanValuePer5Minutes | float64 | Mean value per 5 minutes
meanValuePer15Minutes | float64 | Mean value per 15 minutes
meanValuePerHour | float64 | Mean value per hour
meanValuePer12Hours | float64 | Mean value per 12 hours
meanValuePerDay | float64 | Mean value per day
maxValueLastSecond | float64 | Maximum value for the last second
maxValueLastMinute | float64 | Maximum value for the last minute
maxValueLast5Minutes | float64 | Maximum value for the last 5 minutes
maxValueLast15Minutes | float64 | Maximum value for the last 15 minutes
maxValueLastHour | float64 | Maximum value for the last hour
maxValueLast12Hours | float64 | Maximum value for the last 12 hours
maxValueLastDay | float64 | Maximum value for the last 24 hours
minValueLastSecond | float64 | Minimal value for the last second
minValueLastMinute | float64 | Minimal value for the last minute
minValueLast5Minutes | float64 | Minimal value for the last 5 minutes
minValueLast15Minutes | float64 | Minimal value for the last 15 minutes
minValueLastHour | float64 | Minimal value for the last hour
minValueLast12Hours | float64 | Minimal value for the last 12 hours
minValueLastDay | float64 | Minimal value for the last 24 hours

* **Gauge64** measures values of type `int8`, `int16`, `int32` and `int64`:

Field | Data type | Description
---- | ---- | ----
meanValue | float64 | Mean value for a whole time
maxValue | int64 | Maximum value for a whole time
minValue | int64 | Minimum value for a whole time
lastValue | int64 | Last measured value
deviation | float64 | Standard deviation of all measurements
median | float64 | Percentile 50 of all measurements
percentile90 | float64 | Percentile 90 of all measurements
percentile95 | float64 | Percentile 95 of all measurements
percentile97 | float64 | Percentile 97 of all measurements
meanValuePerSecond | float64 | Mean value per second
meanValuePerMinute | float64 | Mean value per minute
meanValuePer5Minutes | float64 | Mean value per 5 minutes
meanValuePer15Minutes | float64 | Mean value per 15 minutes
meanValuePerHour | float64 | Mean value per hour
meanValuePer12Hours | float64 | Mean value per 12 hours
meanValuePerDay | float64 | Mean value per day
maxValueLastSecond | int64 | Maximum value for the last second
maxValueLastMinute | int64 | Maximum value for the last minute
maxValueLast5Minutes | int64 | Maximum value for the last 5 minutes
maxValueLast15Minutes | int64 | Maximum value for the last 15 minutes
maxValueLastHour | int64 | Maximum value for the last hour
maxValueLast12Hours | int64 | Maximum value for the last 12 hours
maxValueLastDay | int64 | Maximum value for the last 24 hours
minValueLastSecond | int64 | Minimal value for the last second
minValueLastMinute | int64 | Minimal value for the last minute
minValueLast5Minutes | int64 | Minimal value for the last 5 minutes
minValueLast15Minutes | int64 | Minimal value for the last 15 minutes
minValueLastHour | int64 | Minimal value for the last hour
minValueLast12Hours | int64 | Minimal value for the last 12 hours
minValueLastDay | int64 | Minimal value for the last 24 hours

* **RatedGaugeFP** measures rate of received `float32`/`float64` values. This gauge is a combination of **RateGauge** and **GaugeFP**.
* **RatedStringGauge** measures rate of received values of type `string`. This gauge is a combination of **RateGauge** and **StringGauge**.
* **RatedGauge64** measures rate of received values of type `int8`, `int16`, `int32` and `int64`. This gauge is a combination of **RateGauge** and **Gauge64**.
* **RatedFlagGauge** measures rate of received `bool` values. This gauge is a combination of **RateGauge** and **FlagGauge**.
* **RatedTimerGauge** measures rate of received actions. This gauge is a comination of **RateGauge** and **Timer**.
* **RangedGaugeFP** measures `float32`/`float64` values in context of user-defined range of values. This gauge is a combination of **RatedGaugeFP** and **RangedGauge**.
* **RangedGauge64** measures values of type `int8`, `int16`, `int32` and`int64` in context of user-defined range of values. This gauge is a combination of **RatedGauge64** and **RangedGauge**.
* **RangedTimer** measures timing of actions in context of user-defined range of possible durations. This gauge is a combination of **RateGauge** and **Timer**.
* **ArrivalsGauge** measures input requests from perspective of technical SLA parameters like availability. This type of gauge is a combination of **RatedTimerGauge** and the following fields:

Field | Data type | Description
---- | ---- | ----
meanAvailabilityLastSecond | float64 | Measures availability (_1 - denial probability_) measured for the last second
meanAvailabilityLastMinute | float64 | Measures availability (_1 - denial probability_) measured for the last minute
meanAvailabilityLast5Minutes | float64 | Measures availability (_1 - denial probability_) measured for the last 5 minutes
meanAvailabilityLast15Minutes | float64 | Measures availability (_1 - denial probability_) measured for the last 15 minutes
meanAvailabilityLastHour | float64 | Measures availability (_1 - denial probability_) measured for the last hour
meanAvailabilityLast12Hours | float64 | Measures availability (_1 - denial probability_) measured for the last 12 hours
meanAvailabilityLastDay | float64 | Measures availability (_1 - denial probability_) measured for the last 24 hours
availability | float64 | Measures instant availability
efficiency | float64 | Measures efficiency of computational resources: summary processing duration of all input requests divided by total uptime
correlation | float64 | Measures correlation between rate of input requests and response time for each request. Range of value is [0..1]. 1 means bad news for horizontal scaling. Zero means that horizontal scaling will be very effective.
