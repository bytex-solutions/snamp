Thread Pools
====
Some Resource Connectors and Gateways support use multi-threaded I/O operations. By default, SNAMP provides single thread pool shared across all connectors and gateways.
The number of dedicated threads available to SNAMP components equal to `availableProcessors * 1.5`.

Generally, SNAMP supports four major configuration of thread pool:
* Limited capacity of the queue, limited count of threads.
* Unlimited capacity of the queue, limited count of threads. In this case you should specify _2147483647_ value for `queueSize` parameter.
* Limited capacity of the queue, unlimited count of threads. In this case you should specify _2147483647_ value for `maxPoolSize`
* Unlimited capacity of the queue, unlimited count of threads. In this case you should specify _2147483647_ value for `maxPoolSize` and `queueSize`.
