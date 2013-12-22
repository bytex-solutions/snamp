package com.snamp.connectors;

import java.util.*;

/**
 * Represents description of the management operation.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface OperationMetadata extends Map<String, String> {

    /**
     * Represents consistency contract for the operation.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static enum Consistency{
        /**
         * The consistency contract for this operation is unknown.
         */
        UNKNOWN,

        /**
         * Operation execution cannot corrupt anything.
         */
        WILL_NOT_CORRUPT_STATE,

        /**
         * Operation execution may corrupt management connector.
         */
        MAY_CORRUPT_CONNECTOR,

        /**
         * Operation execution may corrupt remote Management Information Base.
         */
        MAY_CORRUPT_MIB,

        /**
         * Operation execution may corrupt management target (such as server shutdown and etc.)
         */
        MAY_CORRUPT_TARGET
    }

    /**
     * Gets consistency contract for this operation.
     * @return The consistency contract for this operation.
     */
    public Consistency getExecutionConsistency();

    /**
     * Determines whether the operation has no return type and should be
     * executed asynchronously.
     * @return {@literal true}, if operation has no return type and should be executed
     * asynchronously; otherwise, {@literal false}.
     */
    public boolean isOneWay();

    /**
     * Returns description of the message that is passed as input argument of the operation invocation.
     * <p>
     *     If you need to pass more that one argument in the input message, you should
     *     describe input message type via {@link com.snamp.connectors.ManagementEntityTabularType}.
     * </p>
     * @return A description of the input message; or {@literal null} if operation has no input message.
     */
    public ManagementEntityType getInputMessageType();

    /**
     * Returns description of the operation invocation result.
     * <p>
     *     If you need to return more that one argument in the output message, you should
     *     describe output message type via {@link com.snamp.connectors.ManagementEntityTabularType}.
     * </p>
     * @return A description of the operation invocation result; or {@literal null}, if operation has
     * no return value.
     */
    public ManagementEntityType getOutputMessageType();
}
