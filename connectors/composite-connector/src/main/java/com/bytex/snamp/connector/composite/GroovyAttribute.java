package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import javax.management.openmbean.OpenType;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

import static com.bytex.snamp.connector.composite.CompositeResourceConfigurationDescriptor.parseGroovyPath;

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

    GroovyAttribute(final String name, final ClassLoader classLoader, final Logger logger, final AttributeDescriptor descriptor) throws ScriptException, ResourceException, AbsentCompositeConfigurationParameterException, IOException {
        this(name, createScriptlet(name, classLoader, logger, descriptor), descriptor);
    }

    private static AggregationAttributeScriptlet createScriptlet(final String name, final ClassLoader classLoader, final Logger logger, final AttributeDescriptor descriptor) throws ScriptException, ResourceException, AbsentCompositeConfigurationParameterException, IOException {
        final String[] path = parseGroovyPath(descriptor);
        final ScriptLoader loader = new ScriptLoader(classLoader, logger, path);
        return loader.createScript(descriptor.getName(name), null);
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
