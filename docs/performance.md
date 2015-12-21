SNAMP Performance Tips
====
This page contains information about SNAMP performance tuning.

## Memory calculation
The minimum amount of memory required for single SNAMP process is 512 MB. This amount of memory is enough for launching single resource adapter and one or two resource connectors.

> RAM calculation methodology reflects SNAMP requirements only. So, the total RAM space installed in your hardware must be greater than space required by SNAMP because OS and other daemon processes utilize its own memory. Pay your attention: SNAMP should not swap its memory.

If you have a plan to use up to 10 resource connectors then allocate 1 GB per SNAMP process. Tens of connected resources can be served with 2 GB heap size. In a clustered installation with tens of connected resources we highly recommend to use 4 GB heap size.

## JVM settings
JVM tuning aims to minimize GC pauses. We offer two main strategies on GC tuning:

1. Peak SNAMP performance is the first priority and there are no pause time requirements (or pauses of 1 second or longer are acceptable)
1. SNAMP response time is more important than overall throughput

You may select the most convenient strategy based on your enterprise IT policies.
> In most scenarios, number of monitoring & management tools are fixed in time (i.e. number of clients are fixed). If that is your case - choose the second strategy.

According to memory calculation methodology you may specify minimum and maximum Java memory:

* For Linux, go to `bin` directory (within the SNAMP folder) and open `bin/setnv` and specify
	* `export JAVA_MIN_MEM=512m`
	* `export JAVA_MAX_MEM=X`, where `X` is your calculated memory, for example `export JAVA_MAX_MEM=2G`
* For Windows, go to `bin` directory (within the SNAMP folder) and open `bin/setenv.bat`
	* `SET JAVA_MIN_MEM=512m`
	* `SET JAVA_MAX_MEM=X`,  where `X` is your calculated memory, for example `SET JAVA_MAX_MEM=2424m`

> We recommend you not to specify PermGen settings

Additional JVM settings can be specified in `EXTRA_JAVA_OPTS` environment parameter. The following example demonstrates setup of _G1GC_ garbage collector:

```bash
export EXTRA_JAVA_OPTS="-XX:+UseG1GC"
```

More information about memory tuning:

* [Java 8 GC tuning](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/index.html)
* [Java 6 GC tuning](http://www.oracle.com/technetwork/java/javase/gc-tuning-6-140523.html)
* [Karaf Tuning](http://karaf.apache.org/manual/latest/users-guide/tuning.html)

### Memory utilization profile
All the Java objects created during SNAMP execution can be divided by its lifecycle:

* Long-lived objects
* Short-lived objects

Short-lived objects are being created for each attribute request or operation execution from monitoring & management tool, and notification delivery to monitoring & management tool. These objects are never being placed into **Old Generation** but require a right tunes size of **Young Generation Heap** (Eden and Survivor spaces). The number of short-lived objects depends on the number of requests per second.

Long-lived objects are being created for each connected managed resource and resource adapter. So, resource adapter instance and resource connector instance are long-lived objects. Changing of SNAMP configuration causes releasing long-lived objects in **Old Generation Heap** (Tenured space). Therefore, if you expect frequent reconfiguring then setup the large OldGen heap (to avoid pauses).

### Throughput first
If peak application performance is the first priority and there are no pause time requirements (or pauses of 1 second or longer are acceptable), then select the parallel collector with `-XX:+UseParallelGC`. You might also specify `-XX:+UseParallelOldGC` if you expect frequent SNAMP reconfiguring.

Also, you can specify `-XX:GCTimeRatio=<N>` JVM option. For example, `-XX:GCTimeRatio=19` sets the goal of 1/20 or 5% of the total time in garbage collection.

### Response time first
If response time is more important than overall throughput and garbage collection pauses must be kept shorter than approximately one second, then you may choose one of the following collectors:

* Concurrent Mark & Sweep `-XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled`. Additionally, specify `-XX:+CMSIncrementalMode` if only two cores are available.
* G1 collector with `-XX:+UseG1GC`. That requires large heaps - about 6GB or larger, and stable and predictable pause time below 0.5 seconds.

## Number of cores
Each resource adapter or resource connector uses its own isolated thread pool.
So, recommended number of cores (k) is based on the following metrics:

* `λ` - expected workload from single monitoring tool, in RPS (requests per second)?
* `t` - average response time from managed resources (in seconds)?
* `P` - availability

SNAMP represents multichannel Queuing System so [Queuing Theory](https://en.wikipedia.org/wiki/Queueing_theory) is applicable for necessary computations:

1. Workload intensity: ![intensity formula](http://latex.codecogs.com/gif.latex?\rho=\lambda\times&space;t)
1. Downtime probability: ![probability formula](http://latex.codecogs.com/gif.latex?p_{0}=\frac{1}{\sum_{i=0}^{k}\frac{\rho^{i}}{i!}})
1. Availability: ![availability formula](http://latex.codecogs.com/gif.latex?P=1-\frac{\rho^{k}}{k!}\rho_{0})

Availability formula contains the required number of cores in indirect form. There is no way to reduce this formula. Therefore, you can use the following simple JS program (use NodeJS or any other JavaScript interpreter) for computing required number of cores:
```js
//source data
var lambda = 50;
var t = 0.1;
var P = 0.999;

//computation
function fact(x){ return x > 1 ? x * fact(x - 1) : 1; }

var rho = lambda * t;

var k = 2;
while(true){
	var sum = 0;
	for(var i = 0; i <= k; i++){
		sum += Math.pow(rho, i) / fact(i);
	}
	sum = 1 / sum;
	sum = sum * Math.pow(rho, k) / fact(k);
	sum = 1 - sum;
	console.log("Availability %s k %s", sum, k);
	if(sum >= P) { console.log("Required number of cores %s", k); return;}
	else k += 1;
}
```

Examples:

1. `λ = 50` rps, `t = 0.1` seconds and expected availability is `P=0,999` (99,9%) then required number of cores `k = 14`
1. `λ = 2` rps, `t = 0.3` seconds and expected availability is `P=0,99` (99%) then required number of cores `k = 4`

> Many modern CPUs support simultaneous multi-threading (SMT) when one physical CPU core may process two (or more) threads in parallel. In this case, `number of cores` means number of logical cores.

## Thread pool size
> It is possible to configure internal thread pool of some resource adapters and connectors. See [Configuration](configuration.md) for more information about thread pool configuration parameters.

Optimal max thread pool size should be equal to ![](http://latex.codecogs.com/gif.latex?S=1.5\times&space;k). Each thread might be used as a separated channel for handling requests.

For example, if workload `λ = 50` rps, `t = 0.1` seconds and expected availability is `P=0,999` (99,9%) then required number of threads  is `S=14`. Therefore, the required number of (logical) cores `k = 14/1.5= [9.3]=10`. The savings on the number of cores is 28%.

## Scalability
SNAMP is a stateless component in your IT infrastructure. Therefore, it is very easy to perform horizontal scalability. Just append the additional nodes into the SNAMP cluster. For more information about SNAMP Cluster configuration, see **Clustering** section in [Installation Guide](installation.md).
