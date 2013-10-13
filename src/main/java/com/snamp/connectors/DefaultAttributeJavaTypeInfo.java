package com.snamp.connectors;

import java.util.Objects;

/**
 * Represents the default implementation of the Java-specific attribute type.
 * @author roman
 */
public class DefaultAttributeJavaTypeInfo implements AttributeJavaTypeInfo {
    private final Class<?> nativeClass;

    /**
     * Initializes a new instance of the Java-specific attribute type.
     * @param nativeClass
     * @param <T>
     */
    public <T> DefaultAttributeJavaTypeInfo(final Class<?> nativeClass){
        if(nativeClass == null) throw new IllegalArgumentException("nativeClass is null.");
        this.nativeClass = nativeClass;
    }

    /**
     * Returns the underlying Java class.
     *
     * @return The underlying Java class.
     */
    @Override
    public final Class<?> getNativeClass() {
        return nativeClass;
    }

    /**
     * Determines whether the attribute value can be converted into the specified type.
     *
     * @param target The result of the conversion.
     * @param <T>    The type of the conversion result.
     * @return {@literal true}, if conversion to the specified type is supported.
     */
    @Override
    public final  <T> boolean canConvertTo(final Class<T> target) {
        return String.class == target || target.isAssignableFrom(nativeClass) || Object.class == target;
    }

    /**
     * Converts the attribute value to the specified type.
     *
     * @param value  The attribute value to convert.
     * @param target The type of the conversion result.
     * @param <T>    Type of the conversion result.
     * @return The conversion result.
     * @throws IllegalArgumentException The target type is not supported.
     */
    @Override
    public final <T> T convertTo(final Object value, final Class<T> target) throws IllegalArgumentException {
        if(target.isInstance(value)) return target.cast(value);
        else if(String.class == target) return (T)Objects.toString(value, "");
        else throw new IllegalArgumentException(String.format("Class %s is not supported", target));
    }

    /**
     * Determines whether the value of the specified type can be passed as attribute value.
     *
     * @param source The type of the value that can be converted to the attribute value.
     * @param <T>    The type of the value.
     * @return {@literal true}, if conversion from the specified type is supported; otherwise, {@literal false}.
     */
    @Override
    public <T> boolean canConvertFrom(final Class<T> source) {
        return nativeClass.isAssignableFrom(source);
    }
}
