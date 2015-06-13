package com.itworks.snamp.adapters.groovy;

import javax.management.MBeanAttributeInfo;

/**
 * Represents attribute processor in functional style.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeValueHandler<I, E extends Throwable> {
    void handle(final String resourceName,
                final MBeanAttributeInfo metadata,
                final I attributeValue) throws E;
}
