package com.bytex.snamp.jmx;

import com.bytex.snamp.Convert;

import javax.management.ImmutableDescriptor;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.bytex.snamp.jmx.DescriptorUtils.DEFAULT_VALUE_FIELD;

/**
 * Describes parameter of the operation.
 * @param <T> Type of the parameter
 * @since 1.1
 * @version 2.0
 */
public final class OpenMBeanParameterInfoSupplier<T> implements Supplier<OpenMBeanParameterInfoSupport> {
    static final class OperationArgumentException extends IllegalArgumentException{
        private static final long serialVersionUID = 1217237225436599211L;

        private OperationArgumentException(final OpenMBeanParameterInfo nullParameter) {
            super(String.format("Parameter %s cannot be null", nullParameter.getName()));
        }

        private OperationArgumentException(final OpenType<?> expectedType){
            super(String.format("Illegal type of the actual argument. Expected type: %s", expectedType));
        }
    }

    private final OpenMBeanParameterInfoSupport parameter;
    private final boolean nullable;

    public OpenMBeanParameterInfoSupplier(final String name,
                                          final String description,
                                          final OpenType<T> openType,
                                          final boolean nullable,
                                          final T defValue) {
        final Map<String, Object> descriptor = new HashMap<>();
        if(defValue != null)
            descriptor.put(DEFAULT_VALUE_FIELD, defValue);
        this.parameter = new OpenMBeanParameterInfoSupport(name, description, openType, new ImmutableDescriptor(descriptor));
        this.nullable = nullable;
    }

    public OpenMBeanParameterInfoSupplier(final String name,
                                          final String description,
                                          final OpenType<T> openType) {
        this(name, description, openType, true);
    }

    public OpenMBeanParameterInfoSupplier(final String name,
                                          final String description,
                                          final OpenType<T> openType,
                                          final boolean nullable) {
        this(name, description, openType, nullable, null);
    }

    private static OpenMBeanParameterInfo[] toParameters(final Stream<OpenMBeanParameterInfoSupplier<?>> suppliers){
        return suppliers.map(OpenMBeanParameterInfoSupplier::get).toArray(OpenMBeanParameterInfo[]::new);
    }

    public static OpenMBeanParameterInfo[] toParameters(final Collection<OpenMBeanParameterInfoSupplier<?>> suppliers){
        return toParameters(suppliers.stream());
    }

    public static OpenMBeanParameterInfo[] toParameters(final OpenMBeanParameterInfoSupplier<?>... suppliers){
        return toParameters(Arrays.stream(suppliers));
    }

    /**
     * Gets MBean operation parameter.
     *
     * @return MBean operation parameter.
     */
    @Override
    public OpenMBeanParameterInfoSupport get() {
        return parameter;
    }

    /**
     * Determines whether value of this parameter can be {@literal null}.
     *
     * @return {@literal true}, if this value of this parameter can be {@literal null}; otherwise, {@literal false}.
     */
    public boolean isNullable() {
        return nullable;
    }

    @SuppressWarnings("unchecked")
    public T getDefaultValue() {
        return (T)parameter.getDefaultValue();
    }

    @SuppressWarnings("unchecked")
    public OpenType<T> getOpenType() {
        return (OpenType<T>) parameter.getOpenType();
    }

    /**
     * Extracts actual value of this parameter from the map of arguments.
     * @param arguments A map of arguments.
     * @return Actual value of this parameter.
     * @throws IllegalArgumentException Incorrect actual value of this parameter.
     */
    public T getArgument(final Map<String, ?> arguments) throws OperationArgumentException {
        if (arguments.containsKey(parameter.getName())) {
            return Convert.toType(arguments.get(parameter.getName()), getOpenType())
                    .orElseThrow(() -> new OperationArgumentException(getOpenType()));
        } else if (isNullable() || getDefaultValue() != null)
            return getDefaultValue();
        else
            throw new OperationArgumentException(parameter);
    }
}
