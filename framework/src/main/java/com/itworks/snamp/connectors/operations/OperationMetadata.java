package com.itworks.snamp.connectors.operations;

import com.itworks.snamp.connectors.ManagedEntityMetadata;
import com.itworks.snamp.connectors.ManagedEntityType;

import java.util.*;

/**
 * Represents description of the management operation.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface OperationMetadata extends ManagedEntityMetadata {

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
     *     describe input message type via {@link com.itworks.snamp.connectors.ManagedEntityTabularType}.
     * </p>
     * @return A description of the input message; or {@literal null} if operation has no input message.
     */
    public ManagedEntityType getInputMessageType();

    /**
     * Returns description of the operation invocation result.
     * <p>
     *     If you need to return more that one argument in the output message, you should
     *     describe output message type via {@link com.itworks.snamp.connectors.ManagedEntityTabularType}.
     * </p>
     * @return A description of the operation invocation result; or {@literal null}, if operation has
     * no return value.
     */
    public ManagedEntityType getOutputMessageType();

    /**
     * Returns the system name of the operation.
     * @return The system name of the operation.
     */
    public String getName();

    /**
     * Returns the localized display name of this operation.
     * @param locale The locale of the display name. If it is {@literal null} then returns display name
     *               in the default locale.
     * @return The localized name of this operation.
     */
    public String getDisplayName(final Locale locale);
}
