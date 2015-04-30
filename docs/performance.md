SNAMP Performance Tips
====

## JVM settings

## RAM calculation

## Number of cores
Each resource adapter or resource connector uses its own isolated thread pool.
So, recommended number of cores based on the following metrics:
* `N` - How many management & monitoring tools connected to SNAMP ?
* `TPS` - What is expected workload from single monitoring tool, in TPS (requests per second)?
* `R` - What is an average response time of managed resources (in seconds)?

Ideally, the number of cores can be calculated using the following formula:
```
Cores = TPS * R * N
```

Note your environment should comply with `TPS ˣ R <= TPS` inequality.
Therefore, the approximate number of cores can be calculated as
```
Cores = TPS * N
```

Of course, this approximation still covers high-load requirements.
If you don't need a high throughput, you can use the following simple rule:

```
Cores = 2 + S(1) + S(2) + ... + S(N)
```

Where
* `S(1)` - the number of cores utilized by the first connected monitoring tool  
* `S(2)` - the number of cores utilized by the second connected monitoring tool
* and etc.

For example, if you have a single monitoring tool with single dedicated core then number of cores
required for SNAMP functionality is equal to `3`.


## Thread pool size
Optimal max thread pool size should be equal to `1.5 * Cores`.
