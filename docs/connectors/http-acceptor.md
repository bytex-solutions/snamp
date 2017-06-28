HTTP Acceptor
====
HTTP Acceptor is a passive Resource Connector that accepts information with monitoring data from **managed resources** using HTTP transport and JSON format. The **managed resources** are responsible for delivering data asynchronously to this connector. This is a perfect solution for environment with numerous microservices. HTTP Acceptor uses this information to produce advanced metrics and statistics. But **managed resource** should use **SNAMP Instrumentation Library** and modify its code for delivering necessary data.

HTTP Acceptor is an implementation on top of [Data Stream Connector](ds-connector.md).
