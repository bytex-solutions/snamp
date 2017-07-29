package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;

import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@FunctionalInterface
interface AttributeSupportProvider {
    Optional<AttributeSupport> getAttributeSupport(final String connectorType);
}
