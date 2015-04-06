package com.itworks.snamp.concurrent;

import com.google.common.base.Stopwatch;
import com.itworks.snamp.TimeSpan;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Represents a simple cache with expiration time for cached value.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class TemporaryCache<I, O, E extends Throwable> {
    private volatile O value = null;
    private final Stopwatch timer;
    private final TimeSpan expirationTime;

    protected TemporaryCache(final TimeSpan expirationTime){
        this.expirationTime = Objects.requireNonNull(expirationTime);
        this.timer = Stopwatch.createUnstarted();
    }

    protected TemporaryCache(final long expirationTime, final TimeUnit expirationTimeUnit){
        this(new TimeSpan(expirationTime, expirationTimeUnit));
    }

    /**
     * Initializes cache.
     * @param input An input object used to initialize cache.
     * @return A new object to be placed into this cache.
     * @throws E Unable to initialize cache.
     */
    protected abstract O init(final I input) throws E;

    /**
     * Cleanup cache.
     * @param input An input object that may be used to cleanup expired value.
     * @param value The expired value.
     * @throws E Unable to cleanup expired value.
     */
    protected abstract void expire(final I input, final O value) throws E;

    private synchronized O getSynchronized(final I input) throws E {
        if(value == null) {
            value = init(input);
            //resets timer
            timer.reset().start();
        }
        return value;
    }

    /**
     * Determines whether this cache is expired.
     * @return {@literal true}, if the value in this cache is expired; otherwise, {@literal false}.
     */
    public final boolean isExpired(){
        final long elapsed = timer.elapsed(expirationTime.unit);
        return elapsed > expirationTime.duration;
    }

    private synchronized void reset(final I input) throws E {
        if(isExpired()) {   //still expired
            expire(input, value);
            value = init(input);
            //resets timer
            timer.reset().start();
        }
    }

    /**
     * Gets cached value.
     * @param input An object used to initialize cache if it is necessary.
     * @return The cached object.
     * @throws E Unable to initialize cache.
     */
    public final O get(final I input) throws E {
        if (value == null) return getSynchronized(input);
        //checks whether the cache is not expired
        if (isExpired())
            reset(input);
        return value;
    }

    /**
     * Sets the cached object to null and return the cached object.
     * @return The cached object.
     */
    protected final synchronized O invalidate(){
        final O value = this.value;
        this.value = null;
        timer.reset();
        return value;
    }
}
