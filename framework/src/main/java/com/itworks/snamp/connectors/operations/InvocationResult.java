package com.itworks.snamp.connectors.operations;

import java.util.concurrent.Future;

/**
 * Represents the state of the operation invocation.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface InvocationResult extends Future<Object> {
    /**
     * Returns the completion status of the operation invocation.
     * @return The completion status of the operation invocation; or {@link com.itworks.snamp.connectors.operations.InvocationStatus#UNCOMPLETED}
     * if invocation is still running.
     */
    public InvocationStatus getStatus();
}
