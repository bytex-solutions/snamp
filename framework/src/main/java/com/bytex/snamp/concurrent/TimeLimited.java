package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents abstract time-based accumulator.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class TimeLimited implements Stateful, Serializable {
    private static final long serialVersionUID = -3419420505943412983L;
    private final AtomicLong timer;

    /**
     * Time-to-live of the value in this accumulator, in millis.
     */
    private final long timeToLive;

    TimeLimited(final Duration ttl){
        timer = new AtomicLong();
        this.timeToLive = Objects.requireNonNull(ttl).toMillis();
        setLocalTime();
    }

    private void setLocalTime(){
        timer.set(System.currentTimeMillis());
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        timer.set(System.currentTimeMillis());
    }

    @SpecialUse        //deserialization hook
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setLocalTime();  //reset timer to the local time
    }

    private boolean resetTimerIfExpired() {
        final long ticks = timer.get();
        final long now = System.currentTimeMillis();
        return now - ticks > timeToLive && timer.compareAndSet(ticks, now);
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
