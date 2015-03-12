package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.WellKnownType;

import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenType;

/**
 * Represents simplified version of {@link javax.management.MBeanAttributeInfo}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class CustomAttributeInfo extends MBeanAttributeInfo {
    private static final long serialVersionUID = 340660963081078107L;

    /**
     * Constructs an <CODE>MBeanAttributeInfo</CODE> object.
     *
     * @param name        The name of the attribute.
     * @param type        The type or class name of the attribute.
     * @param description A human readable description of the attribute.
     * @param specifier Attribute access specifier. Cannot be {@literal null}.
     */
    public CustomAttributeInfo(final String name,
                               final Class<?> type,
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
                               final Class<?> type,
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

    /**
     * Infers type of the attribute.
     * @param attribute The attribute metadata. Cannot be {@literal null}.
     * @return The well-known SNAMP type that should be recognized by resource adapter.
     */
    public static WellKnownType getType(final MBeanAttributeInfo attribute) {
        final OpenType<?> ot = AttributeDescriptor.getOpenType(attribute);
        return ot != null ? WellKnownType.getType(ot) : WellKnownType.getType(attribute.getType());
    }

    /**
     * Returns the descriptor for the feature.  Changing the returned value
     * will have no affect on the original descriptor.
     *
     * @return a descriptor that is either immutable or a copy of the original.
     * @since 1.6
     */
    @Override
    public final AttributeDescriptor getDescriptor() {
        return Utils.safeCast(super.getDescriptor(), AttributeDescriptor.class);
    }
}
