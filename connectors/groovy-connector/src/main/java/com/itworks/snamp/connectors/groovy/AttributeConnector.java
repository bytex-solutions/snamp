package com.itworks.snamp.connectors.groovy;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

/**
 * Represents scripted attribute connector.
 * @author Roman Sakno
 * @version 1.0
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
    AttributeScript loadAttribute(final String scriptFile) throws ResourceException, ScriptException;

    /**
     * Loads attribute described in Groovy script.
     * @param descriptor The attribute descriptor.
     * @return Attribute accessor.
     * @throws ResourceException Unable to find file.
     * @throws ScriptException Invalid script program.
     */
    AttributeScript loadAttribute(final AttributeDescriptor descriptor) throws ResourceException, ScriptException;
}
