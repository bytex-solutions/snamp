package com.snamp.connectors;

/**
 * Represents custom attribute type. There is no guarantee that this type will be supported by SNAMP infrastructure.
 * @param <T> Underlying Java type.
 * @author roman
 */
public interface AttributeJavaTypeInfo<T> extends AttributeTypeInfo {
    /**
     * Returns the underlying Java class.
     * @return The underlying Java class.
     */
    public Class<T> getNativeClass();
}
