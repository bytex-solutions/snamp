package com.bytex.snamp.connectors.attributes;

import javax.management.InvalidAttributeValueException;

/**
 * Supports a simple static set of attributes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class OpenAttributeRepository<T extends OpenMBeanAttributeAccessor> extends AbstractAttributeRepository<T> {
    /**
     * Initializes a new support of management attributes.
     *
     * @param resourceName          The name of the managed resource.
     * @param attributeType         Type of the attribute.
     */
    protected OpenAttributeRepository(final String resourceName,
                                      final Class<T> attributeType) {
        super(resourceName, attributeType);
    }

    /**
     * Connects to the specified attribute.
     *
     * @param attributeName The id of the attribute.
     * @param descriptor  Attribute descriptor.
     * @return The description of the attribute; or {@literal null},
     * @throws Exception Internal connector error.
     */
    @Override
    protected abstract T connectAttribute(final String attributeName,
                                                                 final AttributeDescriptor descriptor) throws Exception;

    /**
     * Obtains the value of a specific attribute of the managed resource.
     *
     * @param metadata The metadata of the attribute.
     * @return The value of the attribute retrieved.
     * @throws Exception Internal connector error.
     */
    @Override
    protected Object getAttribute(final T metadata) throws Exception {
        return metadata.getValue();
    }

    /**
     * Set the value of a specific attribute of the managed resource.
     *
     * @param attribute The attribute of to set.
     * @param value     The value of the attribute.
     * @throws Exception                      Internal connector error.
     * @throws InvalidAttributeValueException Incompatible attribute type.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void setAttribute(final T attribute, final Object value) throws Exception {
        attribute.setValue(value);
    }


}
