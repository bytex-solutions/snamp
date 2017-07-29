package com.bytex.snamp.connector.operations;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.concurrent.LockDecorator;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.AbstractFeatureRepository;
import com.bytex.snamp.connector.metrics.OperationMetric;
import com.bytex.snamp.connector.metrics.OperationMetricRecorder;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;

import javax.annotation.Nonnull;
import javax.management.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents base support of operations.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public abstract class AbstractOperationRepository<M extends MBeanOperationInfo> extends AbstractFeatureRepository<M> implements OperationSupport {
    /**
     * Represents information about operation invocation. This class cannot be inherited or instantiated directly
     * from your code.
     * @param <M> Type of the operation metadata.
     */
    protected static final class OperationCallInfo<M extends MBeanOperationInfo> extends AbstractList<Object> implements DescriptorRead {
        private final M metadata;
        private final Object[] arguments;
        private final String[] signature;

        private OperationCallInfo(final M metadata,
                                  final Object[] args,
                                  final String[] signature){
            this.metadata = Objects.requireNonNull(metadata);
            this.arguments = args != null ? args : emptyArray(Object[].class);
            this.signature = signature != null ? signature : emptyArray(String[].class);
        }

        public String[] getSignature(){
            return signature.clone();
        }

        /**
         * Returns a copy of arguments.
         * @return Copy of array with arguments.
         */
        @Override
        @Nonnull
        public Object[] toArray() {
            return arguments.clone();
        }

        @Override
        @Nonnull
        public <T> T[] toArray(@Nonnull T[] a) {
            if (a.length == 0) a = ObjectArrays.newArray(a, arguments.length);
            System.arraycopy(arguments, 0, a, 0, a.length);
            return a;
        }

        /**
         * Returns a metadata of the operation.
         * @return Metadata of the operation.
         */
        public M getOperation(){
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
            return getOperation().getDescriptor();
        }

        private <K, V> Map<K, V> toMap(final Function<? super MBeanParameterInfo, ? extends K> keyMapper,
                             final Function<Object, ? extends V> valueMapper){
            final Map<K, V> result = Maps.newHashMapWithExpectedSize(arguments.length);
            final MBeanParameterInfo[] parameters = metadata.getSignature();
            for(int i = 0; i < parameters.length; i++)
                result.put(keyMapper.apply(parameters[i]), valueMapper.apply(arguments[i]));
            return result;
        }

        /**
         * Converts this list of arguments into the map of named arguments.
         * @return A set of arguments bounded to its parameters.
         */
        public Map<String, ?> toNamedArguments(){
            return toMap(MBeanParameterInfo::getName, Function.identity());
        }

        /**
         * Converts this list of arguments into the map of named arguments.
         * @return A set of arguments bounded to its parameters.
         */
        public Map<MBeanParameterInfo, ?> toMap(){
            return toMap(Function.identity(), Function.identity());
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
    private final OperationMetricRecorder metrics;
    private final LockDecorator readLock, writeLock;

    protected AbstractOperationRepository(final String resourceName,
                                          final Class<M> metadataType) {
        super(resourceName, metadataType);
        operations = AbstractKeyedObjects.create(MBeanOperationInfo::getName);
        metrics = new OperationMetricRecorder();
        final ReadWriteLock rwLock = new ReentrantReadWriteLock();
        readLock = LockDecorator.readLock(rwLock);
        writeLock = LockDecorator.writeLock(rwLock);
    }

    private void operationAdded(final M metadata){
        fireResourceEvent(OperationModifiedEvent.operationAdded(this, getResourceName(), metadata));
    }

    private void operationRemoved(final M metadata) {
        fireResourceEvent(OperationModifiedEvent.operationRemoving(this, getResourceName(), metadata));
    }

    @Override
    public final OperationMetric getMetrics() {
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
    public final Optional<M> remove(final String operationID) {
        final M metadata = writeLock.apply(this, operationID, AbstractOperationRepository<M>::removeImpl);
        if (metadata == null)
            return Optional.empty();
        else {
            disconnectOperation(metadata);
            return Optional.of(metadata);
        }
    }

    /**
     * Removes operation from the managed resource.
     *
     * @param operationName Name of the operation to remove.
     * @return An instance of removed operation; or {@link Optional#empty()}, if operation with the specified name doesn't exist.
     * @since 2.0
     */
    @Override
    public final Optional<M> removeOperation(final String operationName) {
        return remove(operationName);
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

    protected abstract M connectOperation(final String operationName,
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
    public final Optional<M> enableOperation(final String operationName, final OperationDescriptor descriptor) {
        M result;
        try{
            result = writeLock.call(() -> enableOperationImpl(operationName, descriptor), null);
        } catch (final Exception e) {
            getLogger().log(Level.WARNING, String.format("Failed to enable operation '%s'", operationName), e);
            result = null;
        }
        return Optional.ofNullable(result);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    /**
     * Returns an array of supported operations.
     *
     * @return An array of supported operations.
     */
    @Override
    public final M[] getOperationInfo() {
        return readLock.apply(this, operations.values(), AbstractOperationRepository<M>::toArray);
    }

    /**
     * Returns a metadata of the operation.
     *
     * @param operationID The name of the operation.
     * @return The operation metadata; or {@link Optional#empty()}, if operation doesn't exist.
     */
    @Override
    public final Optional<M> getOperationInfo(final String operationID) {
        return Optional.ofNullable(readLock.apply(operations, operationID, Map::get));
    }

    /**
     * Invokes an operation.
     * @param callInfo Operation call information. Cannot be {@literal null}.
     * @return Invocation result.
     * @throws Exception Unable to invoke operation.
     */
    protected abstract Object invoke(final OperationCallInfo<M> callInfo) throws Exception;

    private Object invoke(final M holder, final Object[] params, final String[] signature) throws Exception {
        return invoke(new OperationCallInfo<>(holder, params, signature));
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
            return readLock.call(() -> {
                final M holder = operations.get(operationName);
                if (holder != null)
                    return invoke(holder, params, signature);
                else
                    throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist", operationName)));
            }, null);
        } catch (final MBeanException | ReflectionException e) {
            throw e;
        } catch (final Exception e) {
            throw new ReflectionException(e);
        } finally {
            metrics.update();
        }
    }

    private void clearImpl() {
        operations.values().forEach(metadata -> {
            operationRemoved(metadata);
            disconnectOperation(metadata);
        });
        operations.clear();
    }

    /**
     * Removes all features from this repository.
     *
     * @since 2.0
     */
    @Override
    public final void clear() {
        writeLock.run(this::clearImpl);
    }

    /**
     * Gets a set of identifiers.
     *
     * @return A set of identifiers.
     */
    @Override
    public final ImmutableSet<String> getIDs() {
        return readLock.apply(operations, ops -> ImmutableSet.copyOf(ops.keySet()));
    }

    @Override
    public final Optional<M> get(final String operationID) {
        return getOperationInfo(operationID);
    }

    @Override
    public final int size() {
        return readLock.supplyInt(operations::size);
    }

    @Override
    @Nonnull
    public final Iterator<M> iterator() {
        return readLock.apply(operations.values(), Collection::iterator);
    }

    @Override
    public final void forEach(final Consumer<? super M> action) {
        readLock.accept(operations.values(), action, Iterable::forEach);
    }

    protected final void failedToExpand(final Level level, final Exception e){
        getLogger().log(level, String.format("Unable to expand operations for resource %s", getResourceName()), e);
    }

    protected final OperationDescriptor createDescriptor(Consumer<OperationConfiguration> initializer) {
        final Consumer<OperationConfiguration> adjustTimeout = config -> config.setInvocationTimeout(OperationConfiguration.TIMEOUT_FOR_SMART_MODE);
        initializer = adjustTimeout.andThen(initializer);
        return createDescriptor(OperationConfiguration.class, initializer, OperationDescriptor::new);
    }

    protected final OperationDescriptor createDescriptor() {
        return createDescriptor(config -> {
        });
    }
}
