package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.scripting.groovy.AbstractAttributeScriptlet;

/**
 * Represents an abstract class for attribute handling script
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class ManagedResourceAttributeScriptlet extends AbstractAttributeScriptlet implements AttributeAccessor {
    /**
     * Releases all resources associated with this attribute.
     *
     * @throws Exception Releases all resources associated with this attribute.
     */
    @Override
    @SpecialUse
    public void close() throws Exception {
        openType = null;
    }

    @Override
    public final AttributeSpecifier specifier() {
        return AttributeSpecifier
                .NOT_ACCESSIBLE
                .writable(isWritable())
                .readable(isReadable());
    }
}
