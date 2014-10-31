package com.itworks.snamp.internal;

import com.itworks.snamp.Box;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.internal.annotations.ThreadSafe;

/**
 * Represents a time-relevant cache.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class TimeBasedCache<T> extends CountdownTimer {
    private final Box<T> valueHolder;
    private final TimeSpan renewalTime;

    /**
     * Represents a new cache value supplier.
     *
     * @param <T> Cache type.
     * @param <E> Type of the exception that can be raised by supplier.
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    public static interface CacheSupplier<T, E extends Throwable> {
        /**
         * Supplies a new value.
         *
         * @param previousValue The previous value.
         * @return A new value for the cache.
         * @throws E An error occurred inside of the supplier.
         */
        T newCacheValue(final T previousValue) throws E;
    }

    /**
     * Initializes a new time-relevant cache.
     *
     * @param renewalTime  Cache renewal time.
     * @param initialValue
     */
    public TimeBasedCache(final TimeSpan renewalTime, final T initialValue) {
        super(renewalTime);
        this.renewalTime = renewalTime;
        valueHolder = new Box<>(initialValue);
    }


    /**
     * Gets or updates the cached value.
     *
     * @param <E>              Type of the exception that can be raised by supplier.
     * @param newValueSupplier Lazy supplier for the new cached value if the current cache is expired.
     * @return The cached value.
     */
    @ThreadSafe
    public final synchronized <E extends Throwable> T getOrRenewCache(final CacheSupplier<T, E> newValueSupplier) throws E {
        if (isEmpty() && stop()) {
            setTimerValue(renewalTime);
            valueHolder.set(newValueSupplier.newCacheValue(valueHolder.get()));
            start();
        }
        return valueHolder.get();
    }
}
