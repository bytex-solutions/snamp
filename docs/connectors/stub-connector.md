Stub Connector
====
Stub Connector is a connector for demonstration and testing purposes. It cannot be used to connect any **managed resource** to SNAMP.

Short list of supported features:

Feature | Comments
---- | ----
Attributes | Only predefined set of attributes
Health checks | Always return **OK**

## Connection String
Connection string is ignored by this connector.

## Configuring attributes
Stub Connector provides the following set of attributes:

Attribute name | Type | Accessors | Description
---- | ---- | ---- | ----
randomInt | int32 | Read-only | Returns randomized integer number with uniform distribution
gaussian | float64 | Read-only | Returns randomized floating-point number with Gaussian distribution
randomBigInteger | bigint | Read-only | Returns randomized integer number with uniform distribution
randomBoolean | bool | Read-only | Returns randomized boolean with uniform distribution
randomBytes | array(int8) | Read-only | Returns randomized array of bytes
intValue | int32 | Read/write | Can be changed with any value
staggeringValue | int32 | Read-only | Gets staggering value in range `[-20, 20]`
