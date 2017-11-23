package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeManager;

import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@FunctionalInterface
interface AttributeSupportProvider {
    Optional<AttributeManager> getAttributeSupport(final String connectorType);
}
