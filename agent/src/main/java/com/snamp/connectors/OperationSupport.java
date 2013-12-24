package com.snamp.connectors;


import com.snamp.TimeSpan;

import java.util.*;
import java.util.concurrent.*;

/**
 * Provides operation invocation support for management connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface OperationSupport {
    /**
     * Represents operation invocation status.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static enum InvocationStatus{
        /**
         * Operation invocation status is unknown, but invocation is completed.
         * <p>
         *     This value is typically used for one-way operations.
         * </p>
         */
        COMPLETED,

        /**
         * Successful operation invocation.
         */
        SUCCESS,

        /**
         * Operation invocation breaks the management connector instance.
         * <p>
         *     In this case, the SNAMP infrastructure should re-create instance.
         * </p>
         */
        COMPLETED_BUT_CONNECTOR_CORRUPTED,

        /**
         * Operation invocation breaks the remote Management Information Base.
         * <p>
         *     Future operation invocation and attribute readings may have undefined
         *     behaviour.
         * </p>
         */
        COMPLETED_BUT_MIB_CORRUPTED,

        /**
         * Operation invocation breaks the management target.
         * <p>
         *     In this case, management target may be unavailable for management connector (for example,
         *     remote server is shutting down).
         * </p>
         */
        COMPLETED_BUT_TARGET_CORRUPTED;

        /**
         * Indicates that the operation is still running.
         */
        public static final InvocationStatus INCOMPLETED = null;
    }

    /**
     * Represents the state of the operation invocation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static interface InvocationResult extends Future<Object>{
        /**
         * Returns the completion status of the operation invocation.
         * @return The completion status of the operation invocation; or {@link com.snamp.connectors.OperationSupport.InvocationStatus#INCOMPLETED}
         * if invocation is still running.
         */
        public InvocationStatus getStatus();
    }

    /**
     * Creates a new invocation queue that is used to execute the specified operations.
     * @param operations A map of operations, where key is a name of the operation and value
     *                   is a map of operation options. Cannot be {@literal null} or empty.
     * @return A new unique identifier of the invocation queue.
     * @throws java.lang.IllegalArgumentException operations is {@literal null} or empty.
     */
    public Object createInvocationQueue(final Map<String, Map<String, String>> operations);

    /**
     * Destroys the specified invocation queue.
     * @param queueId An unique identifier of the invocation queue returned by {@link #createInvocationQueue(java.util.Map)} method.
     * @param force {@literal true} to stop all active executions if timeout is reached; otherwise, {@literal false}.
     * @param timeout Invocation completion timeout.
     * @return {@literal true}, if queue is destroyed successfully; otherwise, {@literal false}.
     * @throws java.lang.IllegalArgumentException Unrecognized queue identifier detected.
     */
    public boolean destroyInvocationQueue(final Object queueId, final boolean force, final TimeSpan timeout);

    /**
     * Returns operation description.
     * @param queueId An unique identifier of the invocation queue returned by {@link #createInvocationQueue(java.util.Map)} method.
     * @param operation The name of the operation to retrieve.
     * @return An information about operation that can be executed inside of the specified queue;
     *  or {@literal null} if operation is not registered in the specified queue.
     */
    public OperationMetadata getOperationInfo(final Object queueId, final String operation);

    /**
     * Enqueue an invocation of the specified operation.
     * @param queueId An unique identifier of the invocation queue returned by {@link #createInvocationQueue(java.util.Map)} method.
     * @param operationName The name of the operation to invoke.
     * @param input An input message.
     * @return An object that provides control over the operation execution process; or {@literal null} if operation is {@link OperationMetadata#isOneWay()}.
     * @throws java.lang.IllegalArgumentException The specified operation is not registered in the queue.
     */
    public InvocationResult invokeOperation(final Object queueId, final String operationName, final Object input);
}
