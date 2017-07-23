package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.management.openmbean.OpenType;
import java.util.Objects;


/**
 * Represents attribute which value can be computed using Groovy script.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyAttribute extends ProcessingAttribute {
    private static final long serialVersionUID = 5143149277819451314L;
    private final AggregationAttributeScriptlet script;

    private GroovyAttribute(final String name, final AggregationAttributeScriptlet scriptlet, final AttributeDescriptor descriptor){
        super(name, scriptlet.type(), "Groovy Scriptlet", scriptlet.isReadable(), scriptlet.isWritable(), false, descriptor);
        script = Objects.requireNonNull(scriptlet);
    }

    GroovyAttribute(final String name, final ScriptLoader loader, final AttributeDescriptor descriptor) throws ScriptException, ResourceException {
        this(name, loader.createScript(descriptor.getAlternativeName().orElse(name), null), descriptor);
    }

    @Override
    Object getValue(final AttributeSupport support) throws Exception {
        return script.getValue(support);
    }

    /**
     * Returns the <i>open type</i> of the values of the parameter
     * described by this <tt>OpenMBeanParameterInfo</tt> instance.
     *
     * @return the open type.
     */
    @Override
    public OpenType<?> getOpenType() {
        return script.type();
    }
}
