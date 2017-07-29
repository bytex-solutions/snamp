package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.google.common.collect.ImmutableMap;
import groovy.lang.Closure;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder for {@link GroovyAttribute}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class GroovyAttributeBuilder extends GroovyFeatureBuilder<AttributeConfiguration> {
    private Closure<?> setter, getter;
    private OpenType<?> attributeType;
    private String description;
    private String name;
    private final Map<String, String> parameters;
    private boolean isIs;

    GroovyAttributeBuilder(){
        attributeType = SimpleType.STRING;
        parameters = ImmutableMap.of();
        isIs = false;
    }

    @Override
    AttributeConfiguration createConfiguration() {
        final AttributeConfiguration configuration = createConfiguration(AttributeConfiguration.class);
        if (!isNullOrEmpty(name))
            configuration.setAlternativeName(name);
        return configuration;
    }

    public void flag(final boolean value){
        isIs = value;
    }

    /**
     * Gets declared name of this attribute.
     * @return Declared attribute name.
     */
    String name(){
        return name;
    }

    public void name(final String value){
        this.name = Objects.requireNonNull(value);
    }

    public void type(final OpenType<?> value){
        attributeType = Objects.requireNonNull(value);
    }

    public OpenType<?> type(){
        return attributeType;
    }

    public void set(final Closure<?> value){
        setter = Objects.requireNonNull(value);
    }

    public void get(final Closure<?> value){
        getter = Objects.requireNonNull(value);
    }

    /**
     * Constructs a new Groovy-based attribute.
     * @param name The name of the attribute.
     * @param descriptor Attribute descriptor.
     * @return Groovy-based attribute.
     */
    GroovyAttribute build(final String name, final AttributeDescriptor descriptor) {
        return new GroovyAttribute(name,
                attributeType,
                isNullOrEmpty(description) ? "Groovy attribute" : description,
                getter,
                setter,
                isIs,
                descriptor);
    }

    GroovyAttribute build() {
        return build(name, new AttributeDescriptor(null, parameters));
    }
}
