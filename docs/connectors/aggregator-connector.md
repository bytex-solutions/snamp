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

## Configuring attributes
Resource Aggregator supplies a predefined set of attributes. Each of them may process one (unary) or two (binary) attributes from another connector (foreign attribute). There is a list of predefined attributes:

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
stringifier | Unary | string | Exposes value of the foreign attribute as a string

The attribute with any other name will no be exposed by connector.

Configuration parameters of the attribute depends on its name:
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
source | string | Yes | The name of the managed resource used as a source of foreign attributes | `app-server`
foreignAttribute | string | Yes | The name of the foreign attribute used as numerator | `memory`
value | string | Yes | User-defined value used as a right in the comparison as a denominator | `42`

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

## Configuring events
