package com.bytex.snamp.concurrent;

import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * Represents time-constrained accumulator for {@code double} numbers.
 * @since 2.0
 * @version 2.0
 */
@ThreadSafe
public abstract class TimeLimitedDouble extends Timeout implements DoubleSupplier, DoubleConsumer, Cloneable {
    private static final long serialVersionUID = 5932747666389586277L;
    private final AtomicDouble current;
    private final double initialValue;

    protected TimeLimitedDouble(final double initialValue, final Duration ttl) {
        super(ttl);
        current = new AtomicDouble(this.initialValue = initialValue);
    }

    protected TimeLimitedDouble(final TimeLimitedDouble source){
        super(source);
        current = new AtomicDouble(source.current.get());
        initialValue = source.initialValue;
    }

    public abstract TimeLimitedDouble clone();

    private void setInitialValue(){
        current.set(initialValue);
    }

    private void resetIfNecessary(){
        acceptIfExpired(this, TimeLimitedDouble::setInitialValue);
    }

    protected final double addAndGet(final double value){
        return current.addAndGet(value);
    }

    protected final double accumulateAndGet(final double value, final DoubleBinaryOperator operator){
        double prev, next;
        do{
            next = operator.applyAsDouble(prev = current.get(), value);
        } while (!current.compareAndSet(prev, next));
        return next;
    }

    protected abstract double accumulate(final double value);

    public final double update(final double value){
        resetIfNecessary();
        return accumulate(value);
    }

    @Override
    public void accept(final double value) {
        update(value);
    }

    @Override
    public double getAsDouble() {
        resetIfNecessary();
        return current.get();
    }

    public static TimeLimitedDouble create(final double initialValue, final Duration ttl, final DoubleBinaryOperator operator){
        final class SimpleTimeLimitedDouble extends TimeLimitedDouble{
            private static final long serialVersionUID = -4958526938290025839L;

            private SimpleTimeLimitedDouble(){
                super(initialValue, ttl);
            }

            private SimpleTimeLimitedDouble(final SimpleTimeLimitedDouble source){
                super(source);
            }

            @Override
            public SimpleTimeLimitedDouble clone() {
                return new SimpleTimeLimitedDouble(this);
            }

            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(value, operator);
            }
        }

        return new SimpleTimeLimitedDouble();
    }

    public static TimeLimitedDouble adder(final double initialValue, final Duration ttl) {
        final class Adder extends TimeLimitedDouble{
            private static final long serialVersionUID = 8102533948810260223L;

            private Adder(){
                super(initialValue, ttl);
            }

            private Adder(final Adder source){
                super(source);
            }

            @Override
            public Adder clone() {
                return new Adder(this);
            }

            @Override
            protected double accumulate(final double value) {
                return addAndGet(value);
            }
        }

        return new Adder();
    }

    public static TimeLimitedDouble peak(final double initialValue, final Duration ttl){
        final class PeakDetector extends TimeLimitedDouble{
            private static final long serialVersionUID = -3373087026614450343L;

            private PeakDetector(){
                super(initialValue, ttl);
            }

            private PeakDetector(final PeakDetector source){
                super(source);
            }

            @Override
            public PeakDetector clone() {
                return new PeakDetector(this);
            }

            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(value, Math::max);
            }
        }

        return new PeakDetector();
    }

    public static TimeLimitedDouble min(final double initialValue, final Duration ttl){
        final class MinValueDetector extends TimeLimitedDouble{
            private static final long serialVersionUID = 1548315628295478141L;

            private MinValueDetector(){
                super(initialValue, ttl);
            }

            private MinValueDetector(final MinValueDetector source){
                super(source);
            }

            @Override
            public MinValueDetector clone() {
                return new MinValueDetector(this);
            }

            @Override
            protected double accumulate(final double value) {
                return accumulateAndGet(value, Math::min);
            }
        }

        return new MinValueDetector();
    }
}
