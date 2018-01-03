package com.bytex.snamp.connector.operations;

import com.bytex.snamp.ResettableIterator;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.connector.FeatureRepository;
import com.bytex.snamp.connector.metrics.OperationMetrics;
import com.bytex.snamp.connector.metrics.OperationMetricsRecorder;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;

import javax.annotation.Nonnull;
import javax.management.*;
import java.beans.IntrospectionException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Represents repository of operations.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
public class OperationRepository<F extends MBeanOperationInfo> extends FeatureRepository<F> {
    private static final long serialVersionUID = 5835542312866735530L;
    /**
     * Represents information about operation invocation. This class cannot be inherited or instantiated directly
     * from your code.
     * @param <M> Type of the operation metadata.
     */
    public static final class OperationCallInfo<M extends MBeanOperationInfo> extends AbstractList<Object> implements DescriptorRead {
        private final M metadata;
        private final Object[] arguments;
        private final String[] signature;

        private OperationCallInfo(final M metadata,
                                  final Object[] args,
                                  final String[] signature) {
            this.metadata = Objects.requireNonNull(metadata);
            this.arguments = args != null ? args : emptyArray(Object[].class);
            this.signature = signature != null ? signature : emptyArray(String[].class);
        }

        public String[] getSignature() {
            return signature.clone();
        }

        /**
         * Returns a copy of arguments.
         *
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
         *
         * @return Metadata of the operation.
         */
        public M getOperation() {
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
                                       final Function<Object, ? extends V> valueMapper) {
            final Map<K, V> result = Maps.newHashMapWithExpectedSize(arguments.length);
            final MBeanParameterInfo[] parameters = metadata.getSignature();
            for (int i = 0; i < parameters.length; i++)
                result.put(keyMapper.apply(parameters[i]), valueMapper.apply(arguments[i]));
            return result;
        }

        /**
         * Converts this list of arguments into the map of named arguments.
         *
         * @return A set of arguments bounded to its parameters.
         */
        public Map<String, ?> toNamedArguments() {
            return toMap(MBeanParameterInfo::getName, Function.identity());
        }

        /**
         * Converts this list of arguments into the map of named arguments.
         *
         * @return A set of arguments bounded to its parameters.
         */
        public Map<MBeanParameterInfo, ?> toMap() {
            return toMap(Function.identity(), Function.identity());
        }

        /**
         * Gets argument value by parameter name.
         *
         * @param parameterName The name of the parameter.
         * @return The argument value.
         * @throws IllegalArgumentException Invalid parameter name.
         */
        public Object get(final String parameterName) throws IllegalArgumentException {
            final MBeanParameterInfo[] parameters = metadata.getSignature();
            for (int i = 0; i < parameters.length; i++) {
                final MBeanParameterInfo par = parameters[i];
                if (Objects.equals(parameterName, par.getName()))
                    return arguments[i];
            }
            throw new IllegalArgumentException(String.format("Parameter '%s' doesn't exist", parameterName));
        }

        /**
         * Gets argument value by parameter name.
         *
         * @param parameterName The name of the parameter.
         * @param expectedType  The expected type of the argument. Cannot be {@literal null}.
         * @return The argument value.
         * @throws IllegalArgumentException Invalid parameter name.
         * @throws ClassCastException       Invalid argument type.
         */
        public <T> T get(final String parameterName, final Class<T> expectedType) throws IllegalArgumentException, ClassCastException {
            return expectedType.cast(get(parameterName));
        }

        /**
         * Gets argument value by its index.
         *
         * @param index        The index of the argument.
         * @param expectedType The expected type of argument. Cannot be {@literal null}.
         * @return The value of the argument.
         * @throws IndexOutOfBoundsException Invalid index of the argument.
         * @throws ClassCastException        Invalid argument type.
         */
        public <T> T get(final int index, final Class<T> expectedType) throws IndexOutOfBoundsException, ClassCastException {
            return expectedType.cast(get(index));
        }

        /**
         * Invokes the specified method using this list of arguments.
         *
         * @param _this  A {@literal this} reference.
         * @param method A method to be invoked. Cannot be {@literal null}.
         * @return Invocation result.
         * @throws InvocationTargetException If the specified method throws an exception.
         * @throws IllegalAccessException    If the specified {@code Method} object
         *                                   is enforcing Java language access control and the underlying
         *                                   method is inaccessible.
         */
        public Object invoke(final Object _this,
                             final Method method) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(_this, arguments);
        }

        /**
         * Invokes the specified method using this list of arguments.
         *
         * @param method A method to invoke. Cannot be {@literal null}.
         * @return Invocation result.
         * @throws Throwable Anything thrown by the target method invocation
         */
        public Object invoke(final MethodHandle method) throws Throwable {
            return method.invokeWithArguments(arguments);
        }

        /**
         * Gets argument value by its index.
         *
         * @param index The index of the argument.
         * @return The value of the argument.
         * @throws IndexOutOfBoundsException Invalid index of the argument.
         */
        @Override
        public Object get(final int index) throws IndexOutOfBoundsException {
            return arguments[index];
        }

        /**
         * Returns count of actual arguments.
         *
         * @return Count of actual arguments.
         */
        @Override
        public int size() {
            return arguments.length;
        }

        private boolean equals(final OperationCallInfo<?> other) {
            return Arrays.equals(arguments, other.arguments) &&
                    Objects.equals(metadata, other.metadata);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof OperationCallInfo<?> && equals((OperationCallInfo<?>) other);
        }

        @Override
        public int hashCode() {
            return 31 * Arrays.hashCode(arguments) + metadata.hashCode();
        }

        @Override
        public String toString() {
            return Arrays.toString(arguments);
        }

        @Override
        public void forEach(final Consumer<? super Object> action) {
            for (final Object arg : arguments)
                action.accept(arg);
        }

        @Override
        @Nonnull
        public ResettableIterator<Object> iterator() {
            return ResettableIterator.of(arguments);
        }

        @Override
        @Nonnull
        public Spliterator<Object> spliterator() {
            return Arrays.spliterator(arguments);
        }

        @Override
        public Stream<Object> stream() {
            return Arrays.stream(arguments);
        }

        @Override
        public Stream<Object> parallelStream() {
            return stream().parallel();
        }

        @Override
        public int indexOf(final Object arg) {
            for (int index = 0; index < arguments.length; index++)
                if (Objects.equals(arg, arguments[index]))
                    return index;
            return -1;
        }

        @Override
        public Object set(final int index, final Object element) {
            final Object previous = arguments[index];
            arguments[index] = element;
            return previous;
        }

        @Override
        public int lastIndexOf(final Object arg) {
            int lastIndex = -1;
            for (int index = 0; index < arguments.length; index++)
                if (Objects.equals(arg, arguments[index]))
                    lastIndex = index;
            return lastIndex;
        }

        @Override
        public boolean contains(final Object other) {
            for (final Object arg : arguments)
                if (Objects.equals(other, arg))
                    return true;
            return false;
        }

        @Override
        public boolean isEmpty() {
            return arguments.length == 0;
        }
    }

    /**
     * Provides invocation of management operation.
     * @param <F> Type of operations in repository.
     */
    @FunctionalInterface
    public interface OperationInvoker<F extends MBeanOperationInfo> {
        Object invokeOperation(final OperationCallInfo<F> callInfo) throws Exception;
    }

    private final OperationMetricsRecorder recorder;
    /**
     * Gets metrics about calling of operations.
     */
    public final OperationMetrics metrics;

    public OperationRepository(){
        metrics = recorder = new OperationMetricsRecorder();
    }

    /**
     * Invokes operation stored in this repository
     * @param actionName Name of operation.
     * @param params Operation parameters.
     * @param signature Signature of operation.
     * @param invoker Operation implementation.
     * @return Result of operation invocation.
     * @throws MBeanException Regular exception was thrown by invoker.
     * @throws ReflectionException Reflection-related operation was thrown by invoker.
     */
    public final Object invoke(final String actionName,
                         final Object[] params,
                         final String[] signature,
                         @Nonnull final OperationInvoker<F> invoker) throws MBeanException, ReflectionException {
        try (final SafeCloseable ignored = readLock.acquireLock(null)) {
            final F operation = getResource().get(actionName);
            if (operation != null)
                return invoker.invokeOperation(new OperationCallInfo<>(operation, params, signature));
        } catch (final ReflectiveOperationException | IntrospectionException e) {
            throw new ReflectionException(e);
        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            recorder.update();
        }
        throw new MBeanException(new OperationsException(String.format("Operation %s doesn't exist", actionName)));
    }

    public static Optional<? extends MBeanOperationInfo> findOperation(final String operationName, final MBeanInfo info) {
        return findFeature(info, MBeanInfo::getOperations, operation -> Objects.equals(operationName, operation.getName()));
    }
}
