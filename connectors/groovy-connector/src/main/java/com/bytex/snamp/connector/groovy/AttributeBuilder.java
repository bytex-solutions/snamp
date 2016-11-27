package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import groovy.lang.Closure;

import javax.management.openmbean.OpenType;
import java.util.Objects;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder for Groovy-based attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AttributeBuilder {
    private Closure<?> setter, getter;
    private OpenType<?> attributeType;
    private String description;

    AttributeBuilder(){

    }

    public void description(final String value){
        this.description = value;
    }

    public void type(final OpenType<?> value){
        attributeType = Objects.requireNonNull(value);
    }

    public void setter(final Closure<?> value){
        setter = Objects.requireNonNull(value);
    }

    public void getter(final Closure<?> value){
        getter = Objects.requireNonNull(value);
    }

    GroovyAttribute build(final String name, final AttributeDescriptor descriptor) {
        return new GroovyAttribute(name,
                attributeType,
                isNullOrEmpty(description) ? "Groovy attribute" : description,
                getter,
                setter,
                descriptor);
    }
}
