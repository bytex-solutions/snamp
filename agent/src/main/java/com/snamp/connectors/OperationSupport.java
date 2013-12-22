package com.snamp.connectors;


import java.util.concurrent.Future;

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
         * Operation invocation status is unknown.
         * <p>
         *     This value is typically used for one-way operations.
         * </p>
         */
        UNKNOWN,

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
        CORRUPT_CONNECTOR,

        /**
         * Operation invocation breaks the remote Management Information Base.
         * <p>
         *     Future operation invocation and attribute readings may have undefined
         *     behaviour.
         * </p>
         */
        CORRUPT_MIB,

        /**
         * Operation invocation breaks the management target.
         * <p>
         *     In this case, management target may be unavailable for management connector (for example,
         *     remote server is shutting down).
         * </p>
         */
        CORRUPT_TARGET
    }
}
