package com.bytex.snamp.moa.watching;

/**
 * Represents state of the watching attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum AttributeState {
    /**
     * Attribute value is in normal range.
     */
    OK,

    /**
     * System administrator or DevOps should pay attention on this attribute.
     */
    WARNING,

    /**
     * Attribute value indicates critical state of the managed resource.
     */
    PANIC
}
