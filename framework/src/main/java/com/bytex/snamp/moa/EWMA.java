package com.bytex.snamp.moa;

import com.bytex.snamp.Stateful;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleSupplier;

/**
 * Represents base class for all exponential moving average implementations.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class EWMA extends Average {
    private static final long serialVersionUID = -6446646338156742697L;

    static final class FloatingDecay extends AtomicLong implements DoubleSupplier, Stateful, Cloneable{
        private static final long serialVersionUID = -8596367659294993371L;
        private final long meanLifetimeNanos;

        FloatingDecay(final Duration meanLifetime){
            super(System.nanoTime());
            meanLifetimeNanos = meanLifetime.toNanos();
        }

        private FloatingDecay(final FloatingDecay other){
            super(other.get());
            meanLifetimeNanos = other.meanLifetimeNanos;
        }

        @Override
        public FloatingDecay clone(){
            return new FloatingDecay(this);
        }

        @Override
        public double getAsDouble() {
            final long currentTime = System.nanoTime();
            final long lastUpdateTime = getAndSet(currentTime);
            return computeAlpha(meanLifetimeNanos, currentTime - lastUpdateTime);
        }

        @Override
        public void reset() {
            set(System.nanoTime());
        }
    }

    private static double computeAlpha(final double meanLifetimeNanos, final double measurementIntervalNanos){
        return 1 - Math.exp(-measurementIntervalNanos / meanLifetimeNanos);
    }

    static double computeAlpha(final Duration meanLifetime,
                                       final Duration measurementInterval) {
        return computeAlpha(meanLifetime.toNanos(), measurementInterval.toNanos());
    }
}
