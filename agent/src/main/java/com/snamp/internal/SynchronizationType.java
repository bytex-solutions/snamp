package com.snamp.internal;

import com.snamp.internal.Internal;

/**
 * Represents utility enum that can be used as advice for method call synchronization.
 * <p>You should not use this type directly from your code.</p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public enum SynchronizationType {
    /**
     * Indicates that the target method doesn't require instance lock for invocation.
     */
    NO_LOCK_REQUIRED,

    /**
     * Indicates that the target method requires invoke lock on the instance for safe invocation.
     */
    READ_LOCK,

    /**
     * Indicates that the target method requires exclusive lock on the instance for safe invocation.
     */
    EXCLUSIVE_LOCK,
}
