package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

/**
 * Represents scripted attribute connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface AttributeConnector {
    /**
     * Loads attribute described in Groovy script.
     * @param scriptFile The name of the file with script.
     * @return Attribute accessor.
     * @throws ResourceException Unable to find file.
     * @throws ScriptException Invalid script program.
     */
    AttributeAccessor loadAttribute(final String scriptFile) throws ResourceException, ScriptException;

    /**
     * Loads attribute described in Groovy script.
     * @param attributeName The name of the attribute.
     * @param descriptor The attribute descriptor.
     * @return Attribute accessor.
     * @throws ResourceException Unable to find file.
     * @throws ScriptException Invalid script program.
     */
    AttributeAccessor loadAttribute(final String attributeName, final AttributeDescriptor descriptor) throws ResourceException, ScriptException;
}
