Aggregator Connector
====
Aggregator (or Resource Aggregator) can be used to aggregate management information from other connected managed resources without scripts and other heavyweight settings. This connector can't be used to connect real managed resources.

Short list of supported features:

Feature | Comments
---- | ----
Attributes | Aggregating attributes from other resource connectors
Events | Read attributes from other resource connectors and emit this information as a notification in periodic manner

If you need scripting functionality for more complex scenarions, see [Groovy Connector](groovy-connector).

## Connection String
Resource Aggregator doesn't support connection string. So, any value of connection string will be ignored.

## Configuration parameters
Resource Aggregator recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
notificationFrequency | Integer | No | Frequency of _periodicAttributeQuery_ event, in millis. By default, it is equal to 5 seconds | `10000`

## Configuring attributes
Resource Aggregator supplies predefined set of attributes. Each of them may process one (unary) or two (binary) attributes from another connector (foreign attribute). There is a list of predefined attributes:

Attribute Name | Kind | Attribute Type | Description
---- | ---- | ----
matcher | Unary | bool | Determines whether the value of the foreign attribute matches to the user-defined regular expression
comparisonWith | Unary | bool | Compares the value of the foreign attribute with user-defined value
comparison | Binary | bool | Compares two foreign attributes
percent | Binary | float64 | A percentage is a number or ratio expressed as a fraction of 100 and computed between two foreign attributes
percentFrom | Unary | float64 |  A percentage is a number or ratio expressed as a fraction of 100 and computed between value of the foreign attribute and user-defined value
counter | Unary | int64 | Summarizes value of the foreign attribute during update interval
average | Unary | float64 | Computes average value of the foreign attribute during update interval
peak | Unary | float64 | Shows peak value of the foreign attribute during update interval
decomposer | Unary | string | Extracts value from the dictionary returned by foreign attribute
composer | 0-arity | dictionary | Composes all scalar attributes provided by the foreign resource into a single composite attribute
stringifier | Unary | string | Exposes value of the foreign attribute as a string

The attribute with any other name will no be exposed by connector.

Configuration parameters of the attribute depend on its name:
* _matcher_ = `foreignAttribute` matches to `value`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute as it is declared in the _source_ managed resource | `memory`
value | Regexp | Yes | Regular expression used to check value of the foreign attribute | `[a-Z]+`

* _comparisonWith_ = `foreignAttribute` compare with `value`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute as it is declared in the _source_ managed resource | `memory`
comparer | string | Yes | The type of the comparison that should be performed between value of the foreign attribute and user-defined value | `>=`
value | string | Yes | User-defined value participated in the comparison as right operand | `42`

* _comparison_ = `firstForeignAttribute` compare with `secondForeignAttribute`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
firstForeignAttribute | string | Yes | The name of the foreign attribute used as a left operand in the comparison | `freeMemory`
secondForeignAttribute | string | Yes | The name of the foreign attribute used as a right operand in the comparison | `totalMemory`
comparer | string | Yes | The type of the comparison that should be performed between value of the foreign attribute and user-defined value | `>=`

* _percent_ = 100 * `firstForeignAttribute` / `secondForeignAttribute`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
firstForeignAttribute | string | Yes | The name of the foreign attribute used as a numerator| `freeMemory`
secondForeignAttribute | string | Yes | The name of the foreign attribute used as a denominator | `totalMemory`

* _percentFrom_ = 100 * `firstForeignAttribute` / `value`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute used as a numerator | `memory`
value | string | Yes | User-defined value used as a right in the comparison as a denominator | `42`

* _counter_ = `foreignAttribute(t1)` + `foreignAttribute(t2)` + ... + `foreignAttribute(t)`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute used as a summand | `memory`
timeInterval | Integer | Yes | Accumulator reset interval, in millis

* _average_ = (`foreignAttribute(1)` + `foreignAttribute(2)` + ... + `foreignAttribute(N)`) / `N`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute used as a numerator | `memory`
timeInterval | Integer | Yes | Accumulator reset interval, in millis

* _peak_ = MAX(`foreignAttribute(1)`, `foreignAttribute(2)`, ..., `foreignAttribute(N)`)

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute used in the row | `memory`
timeInterval | Integer | Yes | Accumulator reset interval, in millis

* _decomposer_ - extracts value by its key from the dictionary

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute which supplies dictionary | `stats`
fieldPath | string | Yes | _/_-separated path to the value in the dictionary | `memory/usage`

* _composer_ - composes all scalar attributes provided by the specified foreign resource
Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign scalar attributes | `app-server`

* _stringifier_ - `toString(foreignAttribute)`

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute to be converted into string | `stats`

### Comparsion type
Comparison type should be specified in `comparer` configuration parameter for several attributes. The following table describes possible values for this configuration parameter:

Value | Description
---- | ----
= | Determines whether the left operand is equal to the right operand
== | The same as `=`
!= | Determines whether the left operand is not equal to the right operand
<> | The same as `!=`
\> | Determines whether the left operand is greater than the right operand
\>= | Determines whether the left operand is greater than or equal to the right operand
< | Determines whether the left operand is less to the right operand
<= | Determines whether the left operand is less than or equal to the right operand

### Use Cases
The following use cases simplifies understanding of different aggregations:

Example 1:
* _Precondition_: managed resource `app-server` exposes `hasError` attribute returning `true` if resource is in error state at request time, and `false` otherwise
* _Task_: how to compute a number of errors per second?
* _Solution_: use `counter` attribute in Resource Aggregator with the following settings:
  * source = app-server
  * timeInterval = 1000
  * foreignAttribute = hasError

Example 2:
* _Precondition_: managed resource `app-server` exposes `cpuUtilizatin` attribute returning instant CPU load
* _Task_: how to compute average CPU load at the specified time interval?
* _Solution_: use `average` attribute in Resource Aggregator with the following settings:
  * source = app-server
  * timeInterval = 10000
  * foreignAttribute = cpuUtilization

Example 3:
* _Precondition_: managed resource `app-server` exposes `memoryUsage` attribute returning instant RAM usage
* _Task_: how to compute peak RAM usage at the specified time interval?
* _Solution_: use `peak` attribute in Resource Aggregator with the following settings:
  * source = app-server
  * timeInterval = 10000
  * foreignAttribute = memoryUsage

## Configuring events
Resource Aggregator supplies predefined set of events:

Category | Description
---- | ----
periodicAttributeQuery | Periodically reads value of the foreign attribute and emits value as notification. The query period is defined using _notificationFrequency_ configuration parameter. The notification will not be emitted if reading operation completed unsuccessfully
healthCheck | Periodically attempts to read attribute value. If this action will fail the event will be emitted. The notification will not be emitted if reading operation completed successfully

Configuration parameters of the event depend on its category:
* _periodicAttributeQuery_

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute to read | `memory`

* _healthCheck_

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute to read | `memory`
