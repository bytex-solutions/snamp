package com.itworks.snamp.connectors.operations;


import com.google.common.annotations.Beta;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.views.View;

import java.util.*;

/**
 * Provides operation invocation support for management connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Beta
public interface OperationSupport extends View {

    /**
     * Creates a new invocation queue that is used to apply the specified operations.
     *
     * @param operations A map of operations, where key is a name of the operation and value
     *                   is a map of operation options. Cannot be {@literal null} or empty.
     * @return A new unique identifier of the invocation queue.
     * @throws java.lang.IllegalArgumentException operations is {@literal null} or empty.
     * @throws com.itworks.snamp.connectors.operations.OperationSupportException Internal connector error.
     */
    Object createInvocationQueue(final Map<String, Map<String, String>> operations) throws OperationSupportException;

    /**
     * Destroys the specified invocation queue.
     *
     * @param queueId An unique identifier of the invocation queue returned by {@link #createInvocationQueue(java.util.Map)} method.
     * @param force   {@literal true} to stop all active executions if timeout is reached; otherwise, {@literal false}.
     * @param timeout Invocation completion timeout.
     * @return {@literal true}, if queue is destroyed successfully; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException Unrecognized queue identifier detected.
     */
    boolean destroyInvocationQueue(final Object queueId, final boolean force, final TimeSpan timeout);

    /**
     * Returns operation description.
     *
     * @param queueId   An unique identifier of the invocation queue returned by {@link #createInvocationQueue(java.util.Map)} method.
     * @param operation The name of the operation to retrieve.
     * @return An information about operation that can be executed inside of the specified queue;
     * or {@literal null} if operation is not registered in the specified queue.
     */
    OperationMetadata getOperationInfo(final Object queueId, final String operation);

    /**
     * Enqueue an invocation of the specified operation.
     *
     * @param queueId       An unique identifier of the invocation queue returned by {@link #createInvocationQueue(java.util.Map)} method.
     * @param operationName The name of the operation to invoke.
     * @param input         An input message.
     * @return An object that provides control over the operation execution process; or {@literal null} if operation is {@link OperationMetadata#isOneWay()}.
     * @throws com.itworks.snamp.connectors.operations.UnknownOperationException The specified operation is not registered in the queue.
     */
    InvocationResult invokeOperation(final Object queueId, final String operationName, final Object input) throws UnknownOperationException;
}
