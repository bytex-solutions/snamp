package com.bytex.snamp.gateway.groovy;

import javax.management.MBeanAttributeInfo;

/**
 * Represents attribute processor in functional style.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface AttributeValueHandler<I, E extends Throwable> {
    void handle(final String resourceName,
                final MBeanAttributeInfo metadata,
                final I attributeValue) throws E;

    static <I, E extends Throwable> AttributeValueHandler<I, E> empty() {
        return (resourceName, metadata, attributeValue) -> {
        };
    }
}
