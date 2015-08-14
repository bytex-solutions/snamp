package com.bytex.snamp.connectors.operations;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractFeatureModeler;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.internal.annotations.MethodStub;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents base support of operations.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractOperationSupport<M extends MBeanOperationInfo> extends AbstractFeatureModeler<M> implements OperationSupport {
    private enum AOSResource{
        OPERATIONS,
        RESOURCE_EVENT_LISTENERS
    }

    /**
     * Represents information about operation invocation. This class cannot be inherited or instantiated directly
     * from your code.
     * @param <M> Type of the operation metadata.
     */
    protected static final class OperationCallInfo<M extends MBeanOperationInfo> extends AbstractList<Object> implements DescriptorRead, Supplier<Map<String, ?>>{
        private final M metadata;
        private final Object[] arguments;

        private OperationCallInfo(final M metadata,
                                  final Object[] args){
            this.metadata = Objects.requireNonNull(metadata);
            this.arguments = args != null ? args : new Object[0];
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

    private static final class OperationHolder<M extends MBeanOperationInfo> extends FeatureHolder<M>{
        private OperationHolder(final M metadata,
                                final String operationName,
                                final CompositeData options){
            super(metadata, computeIdentity(operationName, options));
        }

        private String getOperationName(){
            return getMetadata().getName();
        }

        private boolean equals(final String operationName,
                               final CompositeData options){
            return super.identity.equals(computeIdentity(operationName, options));
        }

        private static BigInteger computeIdentity(final String operationName,
                                                  final CompositeData options) {
            BigInteger result = toBigInteger(operationName);
            for (final String propertyName : options.getCompositeType().keySet())
                result = result.xor(toBigInteger(propertyName))
                        .xor(BigInteger.valueOf(options.get(propertyName).hashCode()));
            return result;
        }
    }

    private final KeyedObjects<String, OperationHolder<M>> operations;

    protected AbstractOperationSupport(final String resourceName,
                                                           final Class<M> metadataType) {
        super(resourceName, metadataType, AOSResource.class, AOSResource.RESOURCE_EVENT_LISTENERS);
        operations = createOperations();
    }

    private static <M extends MBeanOperationInfo> KeyedObjects<String, OperationHolder<M>> createOperations(){
        return new AbstractKeyedObjects<String, OperationHolder<M>>(10) {
            private static final long serialVersionUID = 6753355822109787406L;

            @Override
            public String getKey(final OperationHolder<M> holder) {
                return holder.getOperationName();
            }
        };
    }

    private void operationAdded(final M metadata){
        fireResourceEvent(new OperationAddedEvent(this, getResourceName(), metadata));
    }

    private void operationRemoved(final M metadata){
        fireResourceEvent(new OperationRemovingEvent(this, getResourceName(), metadata));
    }

    @MethodStub
    protected void disableOperation(final M metadata){
    }

    /**
     * Disables management operation.
     * @param operationID The custom-defined name of the operation.
     * @return The metadata of deleted operation.
     */
    @Override
    public final M remove(final String operationID) {
        final OperationHolder<M> holder;
        try (final LockScope ignored = beginWrite(AOSResource.OPERATIONS)) {
            holder = operations.get(operationID);
            if(holder != null){
                operationRemoved(holder.getMetadata());
                operations.remove(operationID);
            }
        }
        if(holder != null){
            disableOperation(holder.getMetadata());
            return holder.getMetadata();
        }
        else return null;
    }

    protected abstract M enableOperation(final String userDefinedName,
                                         final OperationDescriptor descriptor) throws Exception;

    /**
     * Enables management operation.
     * @param userDefinedName Custom-defined name of the management operation
     * @param operationName The name of the operation as it is declared in the resource.
     * @param invocationTimeout Max duration operation invocation.
     * @param options Operation execution options.
     * @return The metadata of enabled operation; or {@literal null}, if operation is not available.
     */
    public final M enableOperation(final String userDefinedName,
                                   final String operationName,
                                   final TimeSpan invocationTimeout,
                                   final CompositeData options){
        OperationHolder<M> holder;
        try(final LockScope ignored = beginWrite(AOSResource.OPERATIONS)){
            holder = operations.get(userDefinedName);
            if(holder != null)
                if(holder.equals(operationName, options))
                    return holder.getMetadata();
                else { //remove operation
                    operationRemoved(holder.getMetadata());
                    holder = operations.remove(userDefinedName);
                    //and register again
                    disableOperation(holder.getMetadata());
                    final M metadata = enableOperation(userDefinedName, new OperationDescriptor(operationName, invocationTimeout, options));
                    if (metadata != null) {
                        operations.put(holder = new OperationHolder<>(metadata, operationName, options));
                        operationAdded(holder.getMetadata());
                    }
                }
            else {
                final M metadata = enableOperation(userDefinedName, new OperationDescriptor(operationName, invocationTimeout, options));
                if(metadata != null) {
                    operations.put(holder = new OperationHolder<>(metadata, operationName, options));
                    operationAdded(holder.getMetadata());
                }
                else holder = null;
            }
        }
        catch (final Exception e) {
            failedToEnableOperation(userDefinedName, operationName, e);
            holder = null;
        }
        return holder != null ? holder.getMetadata() : null;
    }

    /**
     * Reports an error when enabling operation.
     * @param logger The logger instance. Cannot be {@literal null}.
     * @param logLevel Logging level.
     * @param userDefinedName User-defined name of the operation.
     * @param operationName The name of the operation as it is declared in the resource.
     * @param e Internal connector error.
     */
    protected static void failedToEnableOperation(final Logger logger,
                                                      final Level logLevel,
                                                      final String userDefinedName,
                                                      final String operationName,
                                                      final Exception e){
        logger.log(logLevel, String.format("Failed to enable operation '%s' with name '%s'",
                operationName, userDefinedName), e);
    }

    /**
     * Reports an error when enabling operation.
     * @param userDefinedName User-defined name of the operation.
     * @param operationName The name of the operation as it is declared in the resource.
     * @param e Internal connector error.
     * @see #failedToEnableOperation(Logger, Level, String, String, Exception)
     */
    protected abstract void failedToEnableOperation(final String userDefinedName,
                                                    final String operationName,
                                                    final Exception e);

    /**
     * Returns an array of supported operations.
     *
     * @return An array of supported operations.
     */
    @Override
    public final M[] getOperationInfo() {
        try(final LockScope ignored = beginRead(AOSResource.OPERATIONS)){
            return toArray(operations.values());
        }
    }

    /**
     * Returns a metadata of the operation.
     *
     * @param operationID The name of the operation.
     * @return The operation metadata; or {@literal null}, if operation doesn't exist.
     */
    @Override
    public final M getOperationInfo(final String operationID) {
        try(final LockScope ignored = beginRead(AOSResource.OPERATIONS)){
            final OperationHolder<M> holder = operations.get(operationID);
            return holder != null ? holder.getMetadata() : null;
        }
    }

    /**
     * Invokes an operation.
     * @param callInfo Operation call information. Cannot be {@literal null}.
     * @return Invocation result.
     * @throws Exception Unable to invoke operation.
     */
    protected abstract Object invoke(final OperationCallInfo<M> callInfo) throws Exception;

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
     * invoking the operation on the specified managed resource; or {@link ListenableFuture}
     * for asynchronous operations.
     * @throws MBeanException      Wraps a <CODE>java.lang.Exception</CODE> thrown by the managed resource.
     * @throws ReflectionException Wraps a <CODE>java.lang.Exception</CODE> thrown while trying to invoke the method
     */
    @Override
    public final Object invoke(final String operationName,
                         final Object[] params,
                         final String[] signature) throws MBeanException, ReflectionException {
        try(final LockScope ignored = beginRead(AOSResource.OPERATIONS)){
            final OperationHolder<M> holder = operations.get(operationName);
            if(holder != null)
                try{
                    return invoke(new OperationCallInfo<>(holder.getMetadata(), params));
                }
                catch (final MBeanException | ReflectionException e){
                    throw e;
                }
                catch (final Exception e){
                    throw new ReflectionException(e);
                }
            else throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist", operationName)));
        }
    }

    /**
     * Disables all operation registered in this collection.
     * @param removeResourceListeners {@literal true} to remove all resource listeners; otherwise, {@literal false}.
     */
    public final void removeAll(final boolean removeResourceListeners) {
        try (final LockScope ignored = beginWrite(AOSResource.OPERATIONS)) {
            for (final OperationHolder<M> holder : operations.values()) {
                operationRemoved(holder.getMetadata());
                disableOperation(holder.getMetadata());
            }
            operations.clear();
        }
        if (removeResourceListeners)
            removeAllResourceEventListeners();
    }

    @Override
    public final boolean isRegistered(final String operationID) {
        try (final LockScope ignored = beginWrite(AOSResource.OPERATIONS)) {
            return operations.containsKey(operationID);
        }
    }

    /**
     * Gets a set of identifiers.
     *
     * @return A set of identifiers.
     */
    @Override
    public final ImmutableSet<String> getIDs() {
        try(final LockScope ignored = beginRead(AOSResource.OPERATIONS)){
            return ImmutableSet.copyOf(operations.keySet());
        }
    }

    @Override
    public final M get(final String operationID) {
        return getOperationInfo(operationID);
    }

    @Override
    public final int size() {
        try (final LockScope ignored = beginWrite(AOSResource.OPERATIONS)) {
            return operations.size();
        }
    }

    @Override
    public final Iterator<M> iterator() {
        return iterator(operations.values());
    }
}
