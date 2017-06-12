package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents class for measuring timeouts.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
public class Timeout implements Stateful, Serializable {
    private static final long serialVersionUID = -3419420505943412983L;
    private final AtomicLong timer;


    /**
     * Minimum possible value of timeout controller.
     */
    public static final Duration MIN_VALUE = Duration.ofNanos(1L);

    /**
     * Time-to-live of the value in this accumulator, in nanos.
     */
    private final long timeoutNanos;

    private Timeout(final long timeoutNanos) {
        if (timeoutNanos < MIN_VALUE.toNanos())
            throw new IllegalArgumentException(String.format("Timeout cannot be less than %s", MIN_VALUE));
        this.timeoutNanos = timeoutNanos;
        setLocalTime(timer = new AtomicLong(0L));
    }

    public Timeout(final Duration ttl){
        this(ttl.toNanos());
    }

    protected Timeout(final Timeout source) {
        timeoutNanos = source.timeoutNanos;
        timer = new AtomicLong(source.timer.get());
    }

    public Timeout(final long timeout, final TimeUnit unit){
        this(unit.toNanos(timeout));
    }

    private static void setLocalTime(final AtomicLong timer){
        timer.set(System.nanoTime());
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void reset() {
        setLocalTime(timer);
    }

    @SpecialUse(SpecialUse.Case.SERIALIZATION)        //deserialization hook
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setLocalTime(timer);  //reset timer to the local time
    }

    protected final Duration getTimeout(){
        return Duration.ofNanos(timeoutNanos);
    }

    /**
     * Determines whether the timeout is reached.
     * @return {@literal true}, if this timeout is reached; otherwise, {@literal false}.
     */
    public final boolean isExpired(){
        return System.nanoTime() - timer.get() > timeoutNanos;
    }

    /**
     * Resets the internal timer if timeout is reached.
     * @return {@literal true}, if this timeout is reached; otherwise {@literal false}.
     * @implNote This method changes the internal state of the object.
     */
    protected final boolean resetTimerIfExpired() {
        final long ticks = timer.get();
        final long now = System.nanoTime();
        return now - ticks > timeoutNanos && timer.compareAndSet(ticks, now);
    }

    public final boolean runIfExpired(final Runnable action){
        final boolean expired;
        if(expired = resetTimerIfExpired())
            action.run();
        return expired;
    }

    public final <I> boolean acceptIfExpired(final I input, final Consumer<? super I> action) {
        final boolean expired;
        if (expired = resetTimerIfExpired())
            action.accept(input);
        return expired;
    }

    public final <I1, I2> boolean acceptIfExpired(final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action){
        final boolean expired;
        if(expired = resetTimerIfExpired())
            action.accept(input1, input2);
        return expired;
    }
}
