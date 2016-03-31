package com.bytex.snamp.connectors.attributes;

import java.nio.Buffer;

/**
 * Represents managed resource attribute of type that derives from {@link java.nio.Buffer}.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class BufferTypedAttributeInfo<B extends Buffer> extends CustomAttributeInfo {
    /**
     * Constructs an <CODE>MBeanAttributeInfo</CODE> object.
     *
     * @param name        The name of the attribute.
     * @param type        The type or class name of the attribute.
     * @param description A human readable description of the attribute.
     * @param specifier   Attribute access specifier. Cannot be {@literal null}.
     * @param descriptor  The descriptor for the attribute.  This may be null
     */
    public BufferTypedAttributeInfo(final String name, final Class<B> type, final String description, final AttributeSpecifier specifier, final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }
}
