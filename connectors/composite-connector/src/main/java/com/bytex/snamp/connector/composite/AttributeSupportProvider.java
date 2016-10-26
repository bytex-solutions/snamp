package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface AttributeSupportProvider {
    AttributeSupport getAttributeSupport(final String connectorType);
}
