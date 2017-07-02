package com.bytex.snamp.moa;

/**
 * Represents a method of aggregation values in the reservoir.
 */
public enum ReduceOperation {
    MAX,
    MIN,
    MEAN,
    MEDIAN,
    PERCENTILE_90,
    PERCENTILE_95,
    PERCENTILE_97,
    SUM
}
