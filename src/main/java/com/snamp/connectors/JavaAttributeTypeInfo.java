package com.snamp.connectors;

/**
 * Represents custom attribute type. There is no guarantee that this type will be supported by SNAMP infrastructure.
 * @author roman
 */
public interface JavaAttributeTypeInfo extends AttributeTypeInfo {
    /**
     * Returns the underlying Java class.
     * @return The underlying Java class.
     */
    public Class<?> getNativeClass();
}
