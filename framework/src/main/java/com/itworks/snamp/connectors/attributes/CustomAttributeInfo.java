package com.itworks.snamp.connectors.attributes;

import javax.management.MBeanAttributeInfo;

/**
 * Represents simplified version of {@link javax.management.MBeanAttributeInfo}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CustomAttributeInfo<T> extends MBeanAttributeInfo {
    /**
     * Constructs an <CODE>MBeanAttributeInfo</CODE> object.
     *
     * @param name        The name of the attribute.
     * @param type        The type or class name of the attribute.
     * @param description A human readable description of the attribute.
     * @param specifier Attribute access specifier. Cannot be {@literal null}.
     */
    public CustomAttributeInfo(final String name,
                               final Class<T> type,
                               final String description,
                               final AttributeSpecifier specifier) {
        this(name, type.getName(), description, specifier);
    }

    /**
     * Constructs an <CODE>MBeanAttributeInfo</CODE> object.
     *
     * @param name        The name of the attribute.
     * @param type        The type or class name of the attribute.
     * @param description A human readable description of the attribute.
     * @param specifier Attribute access specifier. Cannot be {@literal null}.
     * @param descriptor  The descriptor for the attribute.  This may be null
     *                    which is equivalent to an empty descriptor.
     */
    public CustomAttributeInfo(final String name,
                               final Class<T> type,
                               final String description,
                               final AttributeSpecifier specifier,
                               final AttributeDescriptor descriptor) {
        this(name, type.getName(), description, specifier, descriptor);
    }

    CustomAttributeInfo(final String name,
                        final String type,
                        final String description,
                        final AttributeSpecifier specifier) {
        super(name, type, description, specifier.canRead(), specifier.canWrite(), specifier.isFlag());
    }


    CustomAttributeInfo(final String name,
                        final String type,
                        final String description,
                        final AttributeSpecifier specifier,
                        final AttributeDescriptor descriptor) {
        super(name, type, description, specifier.canRead(), specifier.canWrite(), specifier.isFlag(), descriptor);
    }
}
