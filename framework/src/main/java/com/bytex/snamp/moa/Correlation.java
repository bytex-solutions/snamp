package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;
import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
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
public final class Correlation implements DoubleBinaryOperator, Stateful, DoubleSupplier, Serializable, Cloneable {
    private static final long serialVersionUID = 2580693283449732060L;
    private final AtomicDouble sumX;
    private final AtomicDouble sumY;
    private final AtomicDouble prodX;
    private final AtomicDouble prodY;
    private final AtomicDouble prodXY;
    private final AtomicLong count;

    public Correlation(){
        sumX = new AtomicDouble(0D);
        sumY = new AtomicDouble(0D);
        prodX = new AtomicDouble(0D);
        prodY = new AtomicDouble(0D);
        prodXY = new AtomicDouble(0D);
        count = new AtomicLong(0L);
    }

    private Correlation(final Correlation source){
        sumX = new AtomicDouble(source.sumX.get());
        sumY = new AtomicDouble(source.sumY.get());
        prodX = new AtomicDouble(source.prodX.get());
        prodY = new AtomicDouble(source.prodY.get());
        prodXY = new AtomicDouble(source.prodXY.get());
        count = new AtomicLong(source.count.get());
    }

    @Override
    public Correlation clone(){
        return new Correlation(this);
    }

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
