package com.bytex.snamp.moa;

import com.bytex.snamp.concurrent.Timeout;
import com.google.common.util.concurrent.AtomicDouble;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Basic class for average computation service based on count and summary value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class SimpleAverage extends Average {
    private static final long serialVersionUID = -546572133388003368L;

    private static final class AverageTimeout extends Timeout {
        private static final long serialVersionUID = 3007179548415432076L;

        AverageTimeout(final Duration ttl) {
            super(ttl);
        }

        AverageTimeout(final AverageTimeout other){
            super(other);
        }

        private static double getAverage(final double summary, final long count) {
            return count == 0L ? 0D : summary / count;
        }

        double getAverage(final AtomicDouble summary, final AtomicLong count) {
            if (resetTimerIfExpired()) {
                summary.set(0L);
                count.set(0L);
                return 0D;
            } else
                return getAverage(summary.get(), count.get());
        }

        private static BigDecimal getAverage(final BigDecimal summary, final long count, final MathContext context) {
            return count == 0L ? BigDecimal.ZERO : summary.divide(new BigDecimal(count), context);
        }

        BigDecimal getAverage(final AtomicReference<BigDecimal> summary, final AtomicLong count, final MathContext context) {
            if (resetTimerIfExpired()) {
                summary.set(BigDecimal.ZERO);
                count.set(0L);
                return BigDecimal.ZERO;
            } else
                return getAverage(summary.get(), count.get(), context);
        }
    }

    private final AverageTimeout timer;
    private final AtomicLong counter;

    SimpleAverage(final Duration timeout){
        timer = new AverageTimeout(timeout);
        counter = new AtomicLong(0L);
    }

    SimpleAverage(final SimpleAverage other) {
        timer = new AverageTimeout(other.timer);
        counter = new AtomicLong(other.counter.get());
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void reset() {
        timer.reset();
        counter.set(0L);
    }

    @Override
    @Nonnull
    public abstract SimpleAverage clone();

    final void mark(){
        counter.incrementAndGet();
    }

    final double getAverage(final AtomicDouble summary){
        return timer.getAverage(summary, counter);
    }

    final BigDecimal getAverage(final AtomicReference<BigDecimal> summary, final MathContext context) {
        return timer.getAverage(summary, counter, context);
    }
}
