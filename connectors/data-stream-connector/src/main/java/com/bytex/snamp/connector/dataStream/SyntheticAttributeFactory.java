package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface SyntheticAttributeFactory {
    SyntheticAttribute createAttribute(final String name, final AttributeDescriptor descriptor) throws Exception;
}
