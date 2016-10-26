package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import java.util.function.BiFunction;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface MessageDrivenAttributeFactory extends BiFunction<String, AttributeDescriptor, MessageDrivenAttribute<?>> {
}
