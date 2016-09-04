package com.bytex.snamp.concurrent;

import com.bytex.snamp.Stateful;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Represents abstract time-based accumulator.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class TimeLimited implements Stateful {
    private final AtomicLong timer;

    /**
     * Time-to-live of the value in this accumulator, in millis.
     */
    private final LongSupplier timeToLive;

    TimeLimited(final LongSupplier ttl){
        timer = new AtomicLong(System.currentTimeMillis());
        this.timeToLive = ttl;
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        timer.set(System.currentTimeMillis());
    }

    private boolean resetTimerIfExpired() {
        final long ticks = timer.get();
        final long now = System.currentTimeMillis();
        return now - ticks > timeToLive.getAsLong() && timer.compareAndSet(ticks, now);
    }

    final <I> void acceptIfExpired(final I input, final Consumer<? super I> action){
        if(resetTimerIfExpired())
            action.accept(input);
    }

    final <I1, I2> void acceptIfExpired(final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action){
        if(resetTimerIfExpired())
            action.accept(input1, input2);
    }
}
