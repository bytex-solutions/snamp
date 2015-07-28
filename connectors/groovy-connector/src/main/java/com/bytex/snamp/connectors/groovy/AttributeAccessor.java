package com.bytex.snamp.connectors.groovy;

import com.bytex.snamp.connectors.attributes.AttributeSpecifier;

import javax.management.openmbean.OpenType;

/**
 * Represents attribute accessor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeAccessor extends AutoCloseable {
    OpenType<?> type();
    AttributeSpecifier specifier();
    Object getValue() throws Exception;
    Object setValue(final Object value) throws Exception;
}
