package com.bytex.snamp;

/**
 * Represents an interface for all objects with internal state that can be refreshed.
 * @since 2.0
 * @version 2.1
 * @author Roman Sakno
 */
public interface Stateful {
    /**
     * Resets internal state of the object.
     */
    void reset();
}
