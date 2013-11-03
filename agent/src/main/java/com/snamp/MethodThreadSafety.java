package com.snamp;

/**
 * Describes method usage in the multi-threading context.
 * @author roman
 */
public enum MethodThreadSafety {
    /**
     * Indicates that the method can be called from different threads without additional synchronization.
     */
    THREAD_SAFE,

    /**
     * Indicates that the method cannot be called from different threads without additional synchronization.
     */
    THREAD_UNSAFE,

    /**
     * Indicates that the method implements synchronous loop then it blocks the caller thread (may be infinitely).
     */
    LOOP;
}
