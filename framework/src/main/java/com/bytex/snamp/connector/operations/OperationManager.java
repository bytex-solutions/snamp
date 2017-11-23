package com.bytex.snamp.connector.operations;

import javax.management.JMException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Provides support of management operations.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface OperationManager {
    /**
     * Enables management operation.
     * @param operationName The name of operation to be executed on managed resource.
     * @param descriptor Operation invocation options. Cannot be {@literal null}.
     * @throws JMException Unable to create operation.
     * @since 2.0
     */
    void enableOperation(final String operationName, final OperationDescriptor descriptor) throws JMException;

    /**
     * Removes operation from the managed resource.
     * @param operationName Name of the operation to remove.
     * @return {@literal true}, if operation is disabled.
     * @since 2.0
     */
    boolean disableOperation(final String operationName);

    /**
     * Disables all operations except specified in the collection.
     * @param operations A set of operations which should not be disabled.
     * @since 2.0
     */
    void retainOperations(final Set<String> operations);

    /**
     * Discover operations.
     *
     * @return A map of discovered operations that can be enabled using method {@link #enableOperation(String, OperationDescriptor)}.
     * @since 2.0
     */
    default Map<String, OperationDescriptor> discoverOperations(){
        return Collections.emptyMap();
    }
}
