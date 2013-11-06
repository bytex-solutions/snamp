package com.snamp;

/**
 * Represents aggregator.
 * @author roman
 */
public interface Aggregator {
    /**
     * Retrieves the aggregated object.
     * @param objectType Type of the requested object.
     * @param <T> Type of the aggregated object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    public <T> T queryObject(final Class<T> objectType);
}
