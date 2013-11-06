package com.snamp.connectors;

/**
 * Represents mapping between MIB( management information base) specific type system
 * and native JVM type system
 * <p>
 * There is no guarantee that this type will be supported by SNAMP infrastructure.
 * </p>
 * @param <T> Underlying Java type.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface AttributeJavaTypeInfo<T> extends AttributeTypeInfo {
    /**
     * Returns the underlying Java class.
     * @return The underlying Java class.
     */
    public Class<T> getNativeClass();
}
