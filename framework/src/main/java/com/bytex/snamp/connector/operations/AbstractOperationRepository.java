package com.bytex.snamp.connector.operations;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.metrics.OperationMetrics;
import com.bytex.snamp.connector.metrics.OperationMetricsWriter;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import javax.management.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents base support of operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractOperationRepository<M extends MBeanOperationInfo> extends AbstractFeatureRepository<M> implements OperationSupport, SafeCloseable {
    /**
     * Represents information about operation invocation. This class cannot be inherited or instantiated directly
     * from your code.
     * @param <M> Type of the operation metadata.
     */
    protected static final class OperationCallInfo<M extends MBeanOperationInfo> extends AbstractList<Object> implements DescriptorRead, Supplier<Map<String, ?>> {
        private final M metadata;
        private final Object[] arguments;

        private OperationCallInfo(final M metadata,
                                  final Object[] args){
            this.metadata = Objects.requireNonNull(metadata);
            this.arguments = args != null ? args : emptyArray(Object[].class);
        }

        /**
         * Returns a copy of arguments.
         * @return Copy of array with arguments.
         */
        @Override
        public Object[] toArray() {
            return Arrays.copyOf(arguments, arguments.length);
        }

        @SuppressWarnings({"NullableProblems", "SuspiciousSystemArraycopy"})
        @Override
        public <T> T[] toArray(final T[] a) {
            System.arraycopy(arguments, 0, a, 0, a.length);
            return a;
        }

        /**
         * Returns a metadata of the operation.
         * @return Metadata of the operation.
         */
        public M getMetadata(){
            return metadata;
        }

        /**
         * Returns a copy of Descriptor.
         *
         * @return Descriptor associated with the component implementing this interface.
         * The return value is never null, but the returned descriptor may be empty.
         */
        @Override
        public Descriptor getDescriptor() {
            return getMetadata().getDescriptor();
        }

        /**
         * Converts this list of arguments into the map of named arguments.
         * @return A set of arguments bounded to its parameters.
         */
        public Map<String, ?> toNamedArguments(){
            final Map<String, Object> result = Maps.newHashMapWithExpectedSize(arguments.length);
            final MBeanParameterInfo[] parameters = metadata.getSignature();
            for(int i = 0; i < parameters.length; i++)
                result.put(parameters[i].getName(), arguments[i]);
            return result;
        }

        /**
         * Converts this list of arguments into the map of named arguments.
         * @return A set of arguments bounded to its parameters.
         */
        public Map<MBeanParameterInfo, ?> toMap(){
            final Map<MBeanParameterInfo, Object> result = Maps.newHashMapWithExpectedSize(arguments.length);
            final MBeanParameterInfo[] parameters = metadata.getSignature();
            for(int i = 0; i < parameters.length; i++)
                result.put(parameters[i], arguments[i]);
            return result;
        }

        /**
         * Converts this list of arguments into the map of named arguments.
         * @return A set of arguments bounded to its parameters.
         */
        @Override
        public Map<String, ?> get(){
            return toNamedArguments();
        }

        /**
         * Gets argument value by parameter name.
         * @param parameterName The name of the parameter.
         * @return The argument value.
         * @throws IllegalArgumentException Invalid parameter name.
         */
        public Object get(final String parameterName) throws IllegalArgumentException{
            final MBeanParameterInfo[] parameters = metadata.getSignature();
            for(int i = 0; i < parameters.length; i++){
                final MBeanParameterInfo par = parameters[i];
                if(Objects.equals(parameterName, par.getName()))
                    return arguments[i];
            }
            throw new IllegalArgumentException(String.format("Parameter '%s' doesn't exist", parameterName));
        }

        /**
         * Gets argument value by parameter name.
         * @param parameterName The name of the parameter.
         * @param expectedType The expected type of the argument. Cannot be {@literal null}.
         * @return The argument value.
         * @throws IllegalArgumentException Invalid parameter name.
         * @throws ClassCastException Invalid argument type.
         */
        public <T> Object get(final String parameterName, final Class<T> expectedType) throws IllegalArgumentException, ClassCastException{
            return expectedType.cast(get(parameterName));
        }

        /**
         * Invokes the specified method using this list of arguments.
         * @param _this A {@literal this} reference.
         * @param method A method to be invoked. Cannot be {@literal null}.
         * @return Invocation result.
         * @throws InvocationTargetException If the specified method throws an exception.
         * @throws IllegalAccessException If the specified {@code Method} object
         *              is enforcing Java language access control and the underlying
         *              method is inaccessible.
         */
        public Object invoke(final Object _this,
                             final Method method) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(_this, arguments);
        }

        /**
         * Invokes the specified method using this list of arguments.
         * @param method A method to invoke. Cannot be {@literal null}.
         * @return Invocation result.
         * @throws Throwable Anything thrown by the target method invocation
         */
        public Object invoke(final MethodHandle method) throws Throwable {
            return method.invokeWithArguments(arguments);
        }

        /**
         * Gets argument value by its index.
         * @param index The index of the argument.
         * @param expectedType The expected type of argument. Cannot be {@literal null}.
         * @return The value of the argument.
         * @throws IndexOutOfBoundsException Invalid index of the argument.
         * @throws ClassCastException Invalid argument type.
         */
        public <T> T get(final int index, final Class<T> expectedType) throws IndexOutOfBoundsException, ClassCastException{
            return expectedType.cast(get(index));
        }

        /**
         * Gets argument value by its index.
         * @param index The index of the argument.
         * @return The value of the argument.
         * @throws IndexOutOfBoundsException Invalid index of the argument.
         */
        @Override
        public Object get(final int index) throws IndexOutOfBoundsException{
            return arguments[index];
        }

        /**
         * Returns count of actual arguments.
         * @return Count of actual arguments.
         */
        @Override
        public int size() {
            return arguments.length;
        }
    }

    private final KeyedObjects<String, M> operations;
    private final OperationMetricsWriter metrics;
    private final boolean expandable;

    protected AbstractOperationRepository(final String resourceName,
                                          final Class<M> metadataType,
                                          final boolean expandable) {
        super(resourceName, metadataType);
        operations = AbstractKeyedObjects.create(MBeanOperationInfo::getName);
        metrics = new OperationMetricsWriter();
        this.expandable = expandable;
    }

    private void operationAdded(final M metadata){
        fireResourceEvent(new OperationAddedEvent(this, getResourceName(), metadata));
    }

    private void operationRemoved(final M metadata){
        fireResourceEvent(new OperationRemovingEvent(this, getResourceName(), metadata));
    }

    @Override
    public final OperationMetrics getMetrics() {
        return metrics;
    }

    @MethodStub
    protected void disconnectOperation(final M metadata){
    }

    private M removeImpl(final String operationID) {
        final M holder = operations.get(operationID);
        if (holder != null)
            operationRemoved(holder);
        return operations.remove(operationID);
    }

    /**
     * Disables management operation.
     * @param operationID The custom-defined name of the operation.
     * @return The metadata of deleted operation.
     */
    @Override
    public final M remove(final String operationID) {
        final M metadata = writeApply(operationID, this::removeImpl);
        if (metadata != null)
            disconnectOperation(metadata);
        return metadata;
    }

    /**
     * Disables all operations except specified in the collection.
     *
     * @param operations A set of operations which should not be disabled.
     * @since 2.0
     */
    @Override
    public final void retainOperations(final Set<String> operations) {
        retainAll(operations);
    }

    protected abstract M connectOperation(final String userDefinedName,
                                          final OperationDescriptor descriptor) throws Exception;

    private M connectAndAdd(final String operationName, final OperationDescriptor descriptor) throws Exception{
        final M metadata = connectOperation(operationName, descriptor);
        if (metadata != null) {
            operations.put(metadata);
            operationAdded(metadata);
        }
        return metadata;
    }

    private static boolean equals(final MBeanOperationInfo operation, final String name, final Descriptor descriptor){
        return name.equals(operation.getName()) && descriptor.equals(operation.getDescriptor());
    }

    private M enableOperationImpl(final String operationName, final OperationDescriptor descriptor) throws Exception {
        M holder = operations.get(operationName);
        if (holder != null)
            if (equals(holder, operationName, descriptor))
                return holder;
            else { //remove operation
                operationRemoved(holder);
                holder = operations.remove(operationName);
                disconnectOperation(holder);
                //and register again
                holder = connectAndAdd(operationName, descriptor);
            }
        else
            holder = connectAndAdd(operationName, descriptor);
        return holder;
    }

    /**
     * Enables management operation.
     * @param operationName The name of the operation as it is declared in the resource.
     * @param descriptor Operation execution options.
     * @return Metadata of created operation.
     */
    @Override
    public final M enableOperation(final String operationName, final OperationDescriptor descriptor) {
        try{
            return writeCallInterruptibly(() -> enableOperationImpl(operationName, descriptor));
        } catch (final Exception e) {
            failedToEnableOperation(operationName, e);
            return null;
        }
    }

    /**
     * Reports an error when enabling operation.
     * @param logger The logger instance. Cannot be {@literal null}.
     * @param logLevel Logging level.
     * @param operationName The name of the operation as it is declared in the resource.
     * @param e Internal connector error.
     */
    protected static void failedToEnableOperation(final Logger logger,
                                                      final Level logLevel,
                                                      final String operationName,
                                                      final Exception e){
        logger.log(logLevel, String.format("Failed to enable operation '%s'", operationName), e);
    }

    /**
     * Reports an error when enabling operation.
     * @param operationName The name of the operation as it is declared in the resource.
     * @param e Internal connector error.
     * @see #failedToEnableOperation(Logger, Level, String, Exception)
     */
    protected abstract void failedToEnableOperation(final String operationName,
                                                    final Exception e);

    /**
     * Returns an array of supported operations.
     *
     * @return An array of supported operations.
     */
    @Override
    public final M[] getOperationInfo() {
        return readSupply(() -> toArray(operations.values()));
    }

    /**
     * Returns a metadata of the operation.
     *
     * @param operationID The name of the operation.
     * @return The operation metadata; or {@literal null}, if operation doesn't exist.
     */
    @Override
    public final M getOperationInfo(final String operationID) {
        return readApply(operationID, operations::get);
    }

    /**
     * Invokes an operation.
     * @param callInfo Operation call information. Cannot be {@literal null}.
     * @return Invocation result.
     * @throws Exception Unable to invoke operation.
     */
    protected abstract Object invoke(final OperationCallInfo<M> callInfo) throws Exception;

    private Object invoke(final M holder, final Object[] params) throws Exception {
        return invoke(new OperationCallInfo<>(holder, params));
    }

    /**
     * Allows an operation to be invoked on the managed resource.
     *
     * @param operationName The name of the operation to be invoked.
     * @param params        An array containing the parameters to be set when the operation is
     *                      invoked.
     * @param signature     An array containing the signature of the operation. The class objects will
     *                      be loaded through the same class loader as the one used for loading the
     *                      managed resource connector on which the action is invoked.
     * @return The object returned by the operation, which represents the result of
     * invoking the operation on the specified managed resource; or {@link CompletionStage}
     * for asynchronous operations.
     * @throws MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the managed resource.
     * @throws ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public final Object invoke(final String operationName,
                         final Object[] params,
                         final String[] signature) throws MBeanException, ReflectionException {
        try {
            return readCallInterruptibly(() -> {
                final M holder = operations.get(operationName);
                if (holder != null)
                    return invoke(holder, params);
                else
                    throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist", operationName)));
            });
        } catch (final MBeanException | ReflectionException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReflectionException(e);
        } finally {
            metrics.update();
        }
    }

    private void removeAllImpl(final KeyedObjects<String, M> operations) {
        operations.values().forEach(metadata -> {
            operationRemoved(metadata);
            disconnectOperation(metadata);
        });
        operations.clear();
    }

    /**
     * Disables all operation registered in this collection.
     * @param removeResourceListeners {@literal true} to remove all resource listeners; otherwise, {@literal false}.
     */
    public final void removeAll(final boolean removeResourceListeners) {
        writeAccept(operations, this::removeAllImpl);
        if (removeResourceListeners)
            removeAllResourceEventListeners();
    }

    /**
     * Gets a set of identifiers.
     *
     * @return A set of identifiers.
     */
    @Override
    public final ImmutableSet<String> getIDs() {
        return readApply(operations, ops -> ImmutableSet.copyOf(ops.keySet()));
    }

    @Override
    public final M get(final String operationID) {
        return getOperationInfo(operationID);
    }

    @Override
    public final int size() {
        return readSupply(operations::size);
    }

    @Override
    public final Iterator<M> iterator() {
        return readApply(operations.values(), Collection::iterator);
    }

    protected final void failedToExpand(final Logger logger, final Level level, final Exception e){
        logger.log(level, String.format("Unable to expand operations for resource %s", getResourceName()), e);
    }

    /**
     * Populate this repository with operations.
     *
     * @return A collection of registered operations; or empty collection if nothing tot populate.
     */
    @Override
    public Collection<? extends M> expandOperations() {
        return Collections.emptyList();
    }

    /**
     * Determines whether this repository can be populated with operations using call of {@link #expandOperations()}.
     *
     * @return {@literal true}, if this repository can be populated; otherwise, {@literal false}.
     * @since 2.0
     */
    @Override
    public final boolean canExpandOperations() {
        return expandable;
    }

    /**
     * Removes all operations from this repository.
     */
    @Override
    public void close() {
        removeAll(true);
    }
}
