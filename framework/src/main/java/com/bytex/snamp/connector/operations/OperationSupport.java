package com.bytex.snamp.connector.operations;

import com.bytex.snamp.connector.ManagedResourceAggregatedService;

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Provides support of management operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface OperationSupport extends ManagedResourceAggregatedService {
    /**
     * The name of the field in operation descriptor indicating
     * that the operation is asynchronous and returns {@link com.google.common.util.concurrent.ListenableFuture}
     * as invocation result.
     */
    String ASYNC_FIELD = "async";

    /**
     * The name of the field in operation descriptor that holds
     * name of the managed resource operation as it is declared in the configuration.
     */
    String OPERATION_NAME_FIELD = "operationName";

    /**
     * The name of the field in {@link javax.management.Descriptor} which
     * contains {@link java.time.Duration} value.
     */
    String INVOCATION_TIMEOUT_FIELD = "invocationTimeout";

    /**
     * Enables management operation.
     * @param operationName The name of operation to be executed on managed resource.
     * @param descriptor Operation invocation options. Cannot be {@literal null}.
     * @return Metadata of created operation.
     * @since 2.0
     */
    Optional<? extends MBeanOperationInfo> enableOperation(final String operationName, final OperationDescriptor descriptor);

    /**
     * Removes operation from the managed resource.
     * @param operationName Name of the operation to remove.
     * @return An instance of removed operation; or {@link Optional#empty()}, if operation with the specified name doesn't exist.
     * @since 2.0
     */
    Optional<? extends MBeanOperationInfo> removeOperation(final String operationName);

    /**
     * Disables all operations except specified in the collection.
     * @param operations A set of operations which should not be disabled.
     * @since 2.0
     */
    void retainOperations(final Set<String> operations);

    /**
     * Allows an operation to be invoked on the managed resource.
     *
     * @param operationName The name of the operation to be invoked.
     * @param params An array containing the parameters to be set when the operation is
     * invoked.
     * @param signature An array containing the signature of the operation. The class objects will
     * be loaded through the same class loader as the one used for loading the
     * managed resource connector on which the action is invoked.
     *
     * @return  The object returned by the operation, which represents the result of
     * invoking the operation on the specified managed resource; or {@link com.google.common.util.concurrent.ListenableFuture}
     * for asynchronous operations.
     *
     * @exception javax.management.MBeanException  Wraps a <CODE>java.lang.Exception</CODE> thrown by the managed resource.
     * @exception javax.management.ReflectionException  Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    Object invoke(final String operationName, final Object[] params, final String[] signature)
            throws MBeanException, ReflectionException;

    /**
     * Returns an array of supported operations.
     * @return An array of supported operations.
     */
    MBeanOperationInfo[] getOperationInfo();

    /**
     * Returns a metadata of the operation.
     * @param operationName The name of the operation.
     * @return The operation metadata; or {@link Optional#empty()}, if operation doesn't exist.
     */
    Optional<? extends MBeanOperationInfo> getOperationInfo(final String operationName);

    /**
     * Determines whether the operations can be discovered using call of {@link #discoverOperations()} ()}.
     * @return {@literal true}, if discovery is supported; otherwise, {@literal false}.
     * @since 2.0
     */
    default boolean canDiscoverOperations(){
        return false;
    }

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
