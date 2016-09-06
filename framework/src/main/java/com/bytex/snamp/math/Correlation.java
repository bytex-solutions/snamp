package com.bytex.snamp.math;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.ThreadSafe;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleBinaryOperator;

/**
 * Computes linear correlation between numbers in thread safe manner.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public final class Correlation implements DoubleBinaryOperator, Stateful {
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

    private static double addAndGet(final AtomicDouble thiz, final double value){
        double prev, next;
        do {
            prev = thiz.get();
            next = prev + value;
            if(Double.isInfinite(next)) next = value;
        } while (!thiz.compareAndSet(prev, next));
        return next;
    }

    @Override
    public double applyAsDouble(final double x, final double y) {
        final long count = this.count.incrementAndGet();
        final double sumX = addAndGet(this.sumX, x);
        final double sumY = addAndGet(this.sumY, y);
        final double prodX = addAndGet(this.prodX, x * x);
        final double prodY = addAndGet(this.prodY, y * y);
        final double prodXY = addAndGet(this.prodXY, x * y);
        // covariation
        final double cov = prodXY / count - sumX * sumY / (count * count);
        // standard error of x
        final double sigmaX = Math.sqrt(prodX / count - sumX * sumX / (count * count));
        // standard error of y
        final double sigmaY = Math.sqrt(prodY / count - sumY * sumY / (count * count));

        // correlation is just a normalized covariation
        return cov / sigmaX / sigmaY;
    }
}
