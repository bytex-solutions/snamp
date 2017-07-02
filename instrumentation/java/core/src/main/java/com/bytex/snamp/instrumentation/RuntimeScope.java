package com.bytex.snamp.instrumentation;

/**
 * Represents lexical scope controller.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface RuntimeScope extends AutoCloseable {
    /**
     * Exists from lexical scope at runtime.
     */
    @Override
    void close();
}
