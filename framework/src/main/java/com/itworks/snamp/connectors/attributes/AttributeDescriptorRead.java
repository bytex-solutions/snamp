package com.itworks.snamp.connectors.attributes;

import javax.management.DescriptorRead;

/**
 * Interface to read the Descriptor of a management attribute.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeDescriptorRead extends DescriptorRead {
    /**
     * Returns a copy of Descriptor.
     *
     * @return Descriptor associated with the component implementing this interface.
     * The return value is never null, but the returned descriptor may be empty.
     */
    @Override
    AttributeDescriptor getDescriptor();
}
