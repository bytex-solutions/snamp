package com.bytex.snamp.connector.dsp;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface DataStreamDrivenAttributeFactory {
    DataStreamDrivenAttribute createAttribute(final String name, final AttributeDescriptor descriptor) throws Exception;
}
