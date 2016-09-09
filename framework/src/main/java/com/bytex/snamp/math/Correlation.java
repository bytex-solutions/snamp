package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.ThreadSafe;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleSupplier;

/**
 * Computes linear correlation between numbers in thread safe manner.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public final class Correlation implements DoubleBinaryOperator, Stateful, DoubleSupplier {
    private final AtomicDouble sumX = new AtomicDouble(0D);
    private final AtomicDouble sumY = new AtomicDouble(0D);
    private final AtomicDouble prodX = new AtomicDouble(0D);
    private final AtomicDouble prodY = new AtomicDouble(0D);
    private final AtomicDouble prodXY = new AtomicDouble(0D);
    private final AtomicLong count = new AtomicLong(0L);

    @Override
    public void reset() {
        count.set(0L);
        sumX.set(0D);
        sumY.set(0D);
        prodX.set(0D);
        prodY.set(0D);
        prodXY.set(0D);
    }

    private static double addAndGet(final AtomicDouble atomic, final double value){
        double prev, next;
        do {
            prev = atomic.get();
            next = prev + value;
            if(Double.isInfinite(next))
                next = value;
        } while (!atomic.compareAndSet(prev, next));
        return next;
    }

    private static double compute(final long count,
                                  final double sumX,
                                  final double sumY,
                                  final double prodX,
                                  final double prodY,
                                  final double prodXY){
        // covariation
        final double cov = prodXY / count - sumX * sumY / (count * count);
        // standard error of x
        final double sigmaX = Math.sqrt(prodX / count - sumX * sumX / (count * count));
        // standard error of y
        final double sigmaY = Math.sqrt(prodY / count - sumY * sumY / (count * count));

        // correlation is just a normalized covariation
        final double result = cov / sigmaX / sigmaY;
        return Double.isNaN(result) ? 0D : result;
    }

    @Override
    public double applyAsDouble(final double x, final double y) {
        return compute(count.incrementAndGet(),
                addAndGet(sumX, x),
                addAndGet(sumY, y),
                addAndGet(prodX, x * x),
                addAndGet(prodY, y * y),
                addAndGet(prodXY, x * y));
    }

    /**
     * Gets a correlation.
     *
     * @return Correlation.
     */
    @Override
    public double getAsDouble() {
        return compute(count.get(),
                sumX.get(),
                sumY.get(),
                prodX.get(),
                prodY.get(),
                prodXY.get());
    }
}
