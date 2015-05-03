SNAMP Performance Tips
====
This page contains information about SNAMP performance tuning.

## RAM calculation
The minimum RAM requirement is 2 GB. This amount of memory enough for launching single resource adapter and single resource connector.

Recommended amount of memory required by SNAMP depends on number of configured adapters and connectors:
* Single Resource Adapter memory requirement is based on number of configured attributes, notifications and operations in each connected managed resource. Use the following formula: ![Resource Adapter Memory](http://latex.codecogs.com/gif.latex?M=40&plus;\sum_{i=1}^{r}A\times&space;10&plus;N\times&space;5&plus;O), where `M` - amount of memory required by single Resource Adapter (in MB), `r` - number of connected managed resources, `A` - number of configured attributes for `i`-resource, `N` - number of configured notifications of `i`-resource, `O` - number of configured operations of `i`-resource
* Single Resource Connector memory requirement is based on number of configured attributes, notifications and operations. Use the following formula: ![Resource Connector Memory](http://latex.codecogs.com/gif.latex?S=30&plus;A\times&space;6&plus;N\times&space;3&plus;O), where `S` - amount of memory required by single Resource Connector instance (in MB), `A` - number of configured attributes, `N` - number of configured notifications, `O` - number of configured operations
* Recommended amount of memory: ![SNAMP Memory](http://latex.codecogs.com/gif.latex?R=2048&space;&plus;&space;\sum_{i=1}^{r}S_{i}&plus;\sum_{i=1}^{r}M_{i})

For example, we have 1 resource adapter, 2 resource connector (8 attributes 2 notifications in the first connector, 6 attributes and 3 notifications in the second connector):
* Resource adapter memory: `M = 40 + (8 * 10) + (2 * 5) + (6 * 10) + (3 * 5) = 205 MB`
* Resource connectors memory: `S = S1 + S2 = 30 + (8 * 6) + (2 * 3) + 30 + (6 * 8) + (3 * 3) = 171 MB`
* Recommended amount of memory: `R = 2048 + 205 + 171 = 2424 MB = 2.3 GB`

## JVM settings
The goal of JVM tuning is to minimize GC pauses. We offer two main strategies on GC tuning:
1. Peak application performance is the first priority and there are no pause time requirements or pauses of 1 second or longer are acceptable
1. Response time is more important than overall throughput

You should select the most convenient strategy based on your enterprise IT policies.
> In most scenarios, number of monitoring & management tools are fixed in time (i.e. number of clients are fixed). If this is true for your enterprise then choose the second strategy.

See [Garbage Collection Tuning](http://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/index.html) for more details.

### Memory utilization profile
All Java objects created during SNAMP execution can be divided by its lifecycle:
* Long-lived objects
* Short-lived objects

Short-lived objects created for each attribute request or operation execution from monitoring & management tool, and notification delivery to monitoring & management tool. These objects never places into **Old Generation** but require a right tunes size of **Young Generation Heap** (Eden and Survivor spaces). The number of short-lived objects depends on the number of requests per second.

Long-lived objects created for each connected managed resource and resource adapter. So, resource adapter instance and resource connector instance are long-lived objects. Changing of SNAMP configuration causes releasing long-lived objects in **Old Generation Heap** (Tenured space). Therefore, if you expect frequent reconfiguring then setup the large OldGen heap (to avoid pauses).

### Throughput first
If peak application performance is the first priority and there are no pause time requirements, then select the parallel collector with `-XX:+UseParallelGC`. For Java 8, 

### Response time first
fff

## Number of cores
Each resource adapter or resource connector uses its own isolated thread pool.
So, recommended number of cores (k) based on the following metrics:
* `λ` - what is expected workload from single monitoring tool, in TPS (requests per second)?
* `t` - what is an average response time from managed resources (in seconds)?
* `P` - availability

SNAMP is just a multichannel Queuing System so we can use [Queuing Theory](https://en.wikipedia.org/wiki/Queueing_theory) for necessary computations:
1. Workload intensity: ![](http://latex.codecogs.com/gif.latex?\rho=\lambda\times&space;t)
1. Downtime propability: ![](http://latex.codecogs.com/gif.latex?p_{0}=\frac{1}{\sum_{i=0}^{k}\frac{\rho^{i}}{i!}})
1. Availability: ![](http://latex.codecogs.com/gif.latex?P=1-\frac{\rho^{k}}{k!}\rho_{0})

So, availability formula contains the required number of cores in indirect form. There is no way to reduce this formula. Therefore, you can use the following simple JS program (use NodeJS) for computing required number of cores:
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

1. `λ = 50` tps, `t = 0.1` seconds and expected availability is `P=0,999` (99,9%) then required number of cores `k = 14`
1. `λ = 2` tps, `t = 0.3` seconds and expected availability is `P=0,99` (99%) then required number of cores `k = 4`


## Thread pool size
Optimal max thread pool size should be equal to ![](http://latex.codecogs.com/gif.latex?S=1.5\times&space;k). Each thread may be used as a separated channel for handling requests.

For example, if workload `λ = 50` tps, `t = 0.1` seconds and expected availability is `P=0,999` (99,9%) then required number of threads `S=14`. Therefore, the required number of cores `k = 14/1.5= [9.3]=10`. The savings on the number of cores is 28%.
