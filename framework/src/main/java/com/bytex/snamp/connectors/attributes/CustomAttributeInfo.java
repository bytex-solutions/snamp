package com.bytex.snamp.connectors.attributes;

import static com.google.common.base.MoreObjects.firstNonNull;

import javax.management.MBeanAttributeInfo;
import java.util.Objects;

/**
 * Represents simplified version of {@link javax.management.MBeanAttributeInfo}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class CustomAttributeInfo extends MBeanAttributeInfo implements AttributeDescriptorRead {
    private static final long serialVersionUID = 340660963081078107L;
    private final AttributeDescriptor descriptor;

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
                        final AttributeSpecifier specifier,
                        final AttributeDescriptor descriptor) {
        super(name, type, descriptor.getDescription(description), specifier.canRead(), specifier.canWrite(), specifier.isFlag(), descriptor);
        this.descriptor = Objects.requireNonNull(descriptor);
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
        return firstNonNull(descriptor, AttributeDescriptor.EMPTY_DESCRIPTOR);
    }
}
