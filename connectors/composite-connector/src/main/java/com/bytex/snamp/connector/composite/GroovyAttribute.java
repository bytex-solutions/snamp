package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeSupport;

import javax.management.Descriptor;
import javax.management.openmbean.OpenType;

/**
 * Represents attribute which value can be computed using Groovy script.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyAttribute extends ProcessingAttribute {
    private static final long serialVersionUID = 5143149277819451314L;
    private final AttributeScriptImpl script;

    GroovyAttribute(final String name, final String type, final String description, final boolean isReadable, final boolean isWritable, final boolean isIs, final Descriptor descriptor) {
        super(name, type, description, isReadable, isWritable, isIs, descriptor);
        script = null;
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
