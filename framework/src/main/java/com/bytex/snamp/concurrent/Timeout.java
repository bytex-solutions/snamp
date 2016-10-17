package com.bytex.snamp.concurrent;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.ThreadSafe;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;

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
     * Time-to-live of the value in this accumulator, in millis.
     */
    private final long timeout;

    private Timeout(final long timeoutInMillis){
        timeout = timeoutInMillis;
        setLocalTime(timer = new AtomicLong());
    }

    public Timeout(final Duration ttl){
        this(ttl.toMillis());
    }

    private static void setLocalTime(final AtomicLong timer){
        timer.set(System.currentTimeMillis());
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        setLocalTime(timer);
    }

    @SpecialUse        //deserialization hook
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        setLocalTime(timer);  //reset timer to the local time
    }

    /**
     * Determines whether the timeout is reached.
     * @return {@literal true}, if this timeout is reached; otherwise, {@literal false}.
     */
    public final boolean isExpired(){
        return System.currentTimeMillis() - timer.get() > timeout;
    }

    /**
     * Resets the internal timer if timeout is reached.
     * @return {@literal true}, if this timeout is reached; otherwise {@literal false}.
     * @implNote This method changes the internal state of the object.
     */
    protected final boolean resetIfExpired() {
        final long ticks = timer.get();
        final long now = System.currentTimeMillis();
        return now - ticks > timeout && timer.compareAndSet(ticks, now);
    }

    public final <T> Optional<? extends T> supplyIfExpired(final Supplier<? extends T> supplier){
        return resetIfExpired() ? Optional.of(supplier.get()) : Optional.empty();
    }

    public final <I, O> Optional<? extends O> applyIfExpired(final I input, final Function<? super I, ? extends O> fn){
        return resetIfExpired() ? Optional.of(fn.apply(input)) : Optional.empty();
    }

    public final <I> boolean acceptIfExpired(final I input, final Consumer<? super I> action){
        final boolean expired;
        if(expired = resetIfExpired())
            action.accept(input);
        return expired;
    }

    public final <I1, I2> boolean acceptIfExpired(final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action){
        final boolean expired;
        if(expired = resetIfExpired())
            action.accept(input1, input2);
        return expired;
    }
}
