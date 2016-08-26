package com.bytex.snamp.connector.composite.functions;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Computes percentile.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class PercentileFunction extends ToDoubleFunction {
    private final long intervalNanos;
    private final double quantile;
    private long checkpointNanos;
    private final ArrayList<Double> series; //array list is always sorted

    PercentileFunction(final long quantile, final long interval, final TimeUnit unit){
        intervalNanos = unit.toNanos(interval);
        this.quantile = quantile / 100.0;
        checkpointNanos = System.nanoTime();
        series = new ArrayList<>();
    }

    @Override
    synchronized double compute(final double input) {
        if (System.nanoTime() - checkpointNanos > intervalNanos) {
            series.clear();
            checkpointNanos = System.nanoTime();
        }
        switch (series.size()){
            case 0:
                series.add(input);
                return input;
            case 1:
                final double other = series.get(0);
                series.add(input);
                return Math.min(input, other);
            default:
        }
        int low = 0;
        int high = series.size() - 1;
        while (low <= high){
            int midIndex = (high - low) >>> 1;   //(high - low) / 2
            final double midValue = series.get(midIndex);
            final int comparisonResult = Double.compare(input, midValue);
            if(comparisonResult > 0)    //input > midValue
                low = midIndex + 1;
            else
                if(comparisonResult < 1)
                    high = midIndex - 1;
            else {
                series.add(midIndex, input);
                }
        }
        final int index = series.
    }
}
