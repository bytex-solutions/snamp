package com.bytex.snamp.connector.operations;

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.util.Collection;
import java.util.Set;

/**
 * Provides support of management operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface OperationSupport {
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
    MBeanOperationInfo enableOperation(final String operationName, final OperationDescriptor descriptor);

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
     * @return The operation metadata; or {@literal null}, if operation doesn't exist.
     */
    MBeanOperationInfo getOperationInfo(final String operationName);

    /**
     * Determines whether this repository can be populated with operations using call of {@link #expandOperations()}.
     * @return {@literal true}, if this repository can be populated; otherwise, {@literal false}.
     * @since 2.0
     */
    boolean canExpandOperations();

    /**
     * Populate this repository with operations.
     *
     * @return A collection of registered operations; or empty collection if nothing to populate.
     * @since 2.0
     */
    Collection<? extends MBeanOperationInfo> expandOperations();
}
