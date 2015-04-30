SNAMP Performance Tips
====

## JVM settings

## RAM calculation

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

For example, if workload `λ=50` tps, `t=0.1` and expected availability is `P=0,999` (99,9%) then required number of cores `k=14`


## Thread pool size
Optimal max thread pool size should be equal to `1.5 * Cores`.
