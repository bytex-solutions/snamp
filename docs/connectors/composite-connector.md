Composite Connector
====
Composite Connector can be used to aggregate management information from other connected managed resources without any scripting and other heavyweight settings. This connector can't be used to connect real managed resources.

Short list of supported features:

Feature | Comments
---- | ----
Attributes | Aggregating attributes from other resource connectors
Events | Re-transmit notifications from composed resource connectors
Health checks | Aggregates health checks from composed resource connectors
Operations | Pass operation invocation into composed resource connectors

If you need scripting functionality for more complex scenarios, see [Groovy Connector](groovy-connector.md).

## Connection String
Connection string is a combination of all connection strings required by composed resource connector. It has the following format:
```
<connector-type>:<connection-string>[;<connector-type>:<connection-string>...]
```

Example of composition using two connectors: `jmx` and `snmp`:
```
jmx:=service:jmx:rmi:///jndi/rmi://localhost:5657/karaf-root; snmp:=udp://192.168.0.1:25
```

Note that two resource connectors with the same type cannot be composed. Delimiter can be changed by configuration parameter.

## Configuration parameters
Composite Connector recognizes the following parameters:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
separator | String | No | Separator used to split connection strings of composed connectors | `_`
synchronizationPeriod | Integer | No | A period of time (in millis) used to synchronize accumulators with aggregated metrics across cluster. Default is `5000` | `3000`
groovyPath | String | Yes if use `groovy()` as formula for accumulators; otherwise, no | Semicolon-separated list of URL paths that point to directories with Groovy scripts | `file:/opt/snamp/groovy/composition`

## Configuring attributes
Composite Connector offers two types of attributes:
* Proxy attributes that expose from composed resource connectors
* Accumulators which provides additional statistical information about composed attributes. Accumulated values will be automatically synchronized across SNAMP cluster nodes.

### Proxy attributes
Configuration schema for proxy attributes:
* _Name_ - name of attribute provided by composed resource connector/
* Configuration parameters will be passed as-is into composed resource connector. The following configuration parameters used to describe proxy attributes correctly:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
source | String | Yes | Type of resource connector used as a source for attribute | `jmx`

### Accumulators
Configuration schema for accumulator attributes:
* _Name_ - any human-readable name of the attribute.
* Configuration parameters depends on accumulator type but some parameters are generic for all accumulators:

Parameter | Type | Required | Meaning | Example
---- | ---- | ---- | ---- | ----
name | String | Yes | Name of the proxy attribute used by accumulator | `usedMemory`
formula | String | Yes | Formula that describes accumulation | `percentile(95)`

Set of available formulas:

Formula | Description | Example
---- | ---- | ----
max() | Computes maximum value of the attribute | `max()`
min() | Computes minimum value of the attribute | `max()`
gauge_fp() | Provides complex statistics of attribute specified by parameter `name` if it has floating-point value type. See [Management Information Model](../inform_model.md) for more information about gauge of type **GaugeFP**. | `gauge_fp()`
gauge_int() | Provides complex statistics of attribute specified by parameter `name` if it has integer value type. See [Management Information Model](../inform_model.md) for more information about gauge of type **Gauge64**. | `gauge_int()`
ranged_fp(_lower-bound_, _upper-bound_) | The same as `gauge_fp()` but statistics include information about how the attribute value relates to the specified range. Lower bound and upper bound values are inclusive. See [Management Information Model](../inform_model.md) for more information about gauge of type **RangedGaugeFP**. | `ranged_fp(10.0, 20.0)`
ranged_int(_lower-bound_, _upper-bound_) | The same as `gauge_int()` but statistics include information about how the attribute value relates to the specified range. Lower bound and upper bound values are inclusive. See [Management Information Model](../inform_model.md) for more information about gauge of type **RangedGauge64**. | `ranged_int(10, 20)`
avg(_time-interval_) | Computes moving average for the specified period of time | `avg(10s)` average value for 10 seconds. Other possible time units: `s` - seconds, `ns` - nanoseconds, `ms` - milliseconds, `m` - minutes, `h` - hours, `d` - days
percentile(_percentile_) | Computes percentile of attribute | `percentile(50)` returns median value
correl($_attributeName_) | Computes correlation between attribute specified in parameter `name` and other attribute specified as argument | `correl($freeMemory)`
extract($_datatype_, _fieldName_) | Extracts field from complex data type return by attribute. Available data types described in [Management Information Model](../inform_model.md) | `extract(float64, total)`
flag() | Computes statistics about boolean value returned by attribute. See [Management Information Model](../inform_model.md) for more information about gauge of type **FlagGauge**. | `flag()`
rate() | Measures rate of the notification specified in `name` configuration parameter. See [Management Information Model](../inform_model.md) for more information about gauge of type **Rate**. | `rate()`
groovy() | Use custom accumulator written in Groovy scripting language. Configuration parameter `name` contains name of script file with `.groovy` extension and without path | `groovy()`

Example 1:
* _Precondition_: managed resource exposes `cpuUtilizatin` attribute returning instant CPU load
* _Task_: how to detect overloading of CPU?
* _Solution_: use `ranged_fp` in conjunction with `extract`:
  1. Define attribute _cpuUtilStat_:
    * name = cpuUtilization
    * formula = ranged_fp(0, 90)
  2. Define attribute _overloadStat_:
    * name = cpuUtilStat
    * formula = extract(greaterThanRange, float64)

Example 2:
* _Precondition_: managed resource exposes `memoryUsage` attribute returning instant RAM usage
* _Task_: how to compute peak RAM usage?
* _Solution_: use `max` accumulator with the following settings:
  * name = memoryUsage
  * formula = max()

### Using Groovy for accumulators
It is possible to write custom accumulators using Groovy language. Use the following steps to do that:
1. Declare type of the accumulator using `type` function.
1. Declare function `def getValue()` and return the accumulated value from this function
1. Optionally, accumulator can have modifier for its value using declaration of function `def setValue(value)`
1. All other attributes are available as global properties from the script

The following example demonstrates how to implement custom accumulator that infers new value from existing attribute named `integer`:
```groovy
type INT64

def getValue() {
    integer + 10L
}
```

## Configuring events

## Configuring operations

## Health checks
Health check returned by Composite Connector will be computed as a summary health check computed from all composed connectors.
