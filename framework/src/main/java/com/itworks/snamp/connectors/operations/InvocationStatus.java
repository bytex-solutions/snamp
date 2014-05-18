package com.itworks.snamp.connectors.operations;

/**
 * Represents operation invocation status.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public enum InvocationStatus {
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
