package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface MessageDrivenAttributeFactory {
    MessageDrivenAttribute createAttribute(final String name, final AttributeDescriptor descriptor) throws Exception;
}
