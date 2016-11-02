package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.scripting.groovy.AttributeScript;

/**
 * Represents attribute accessor.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface AttributeAccessor extends AttributeScript, AutoCloseable {
    AttributeSpecifier specifier();
}
