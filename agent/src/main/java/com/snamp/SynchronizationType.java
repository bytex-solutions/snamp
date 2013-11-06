package com.snamp;

/**
 * Represents utility enum that can be used as advice for method call synchronization
 * @author roman
 */
public enum SynchronizationType {
    NO_LOCK_REQUIRED,
    READ_LOCK,
    EXCLUSIVE_LOCK,
}
