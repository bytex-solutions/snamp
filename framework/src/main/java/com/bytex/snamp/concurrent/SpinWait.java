package com.bytex.snamp.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Provides support for spin-based waiting.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public final class SpinWait {
    private SpinWait(){
        throw new InstantiationError();
    }

    private static long spin(long timeoutInMillis) throws InterruptedException, TimeoutException {
        final long DELAY = 1;     //sleep time, in millis
        //sleep for next iteration
        Thread.sleep(DELAY);
        timeoutInMillis -= DELAY;
        //timeout exceeded
        if (timeoutInMillis <= 0)
            throw new TimeoutException();
        return timeoutInMillis;
    }

    @Nonnull
    public static <V> V untilNull(final Callable<? extends V> spin, final Duration timeout) throws Exception {
        V result;
        long timeoutInMillis = timeout.toMillis();
        while ((result = spin.call()) == null)
            timeoutInMillis = spin(timeoutInMillis);
        return result;
    }

    @Nonnull
    public static <I, O> O untilNull(final I input, final Function<? super I, ? extends O> spin, final Duration timeout) throws InterruptedException, TimeoutException {
        O result;
        long timeoutInMillis = timeout.toMillis();
        while ((result = spin.apply(input)) == null)
            timeoutInMillis = spin(timeoutInMillis);
        return result;
    }

    @Nonnull
    public static <I1, I2, O> O untilNull(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> spin, final Duration timeout) throws InterruptedException, TimeoutException {
        O result;
        long timeoutInMillis = timeout.toMillis();
        while ((result = spin.apply(input1, input2)) == null)
            timeoutInMillis = spin(timeoutInMillis);
        return result;
    }

    public static void until(final BooleanSupplier condition, final Duration timeout) throws InterruptedException, TimeoutException {
        long timeoutInMillis = timeout.toMillis();
        while (condition.getAsBoolean())
            timeoutInMillis = spin(timeoutInMillis);
    }
}
