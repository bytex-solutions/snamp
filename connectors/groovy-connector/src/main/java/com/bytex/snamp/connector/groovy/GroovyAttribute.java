package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.jmx.JMExceptionUtils;
import groovy.lang.Closure;

import javax.management.ReflectionException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyAttribute extends AbstractOpenAttributeInfo {
    private static final long serialVersionUID = 5604082026422274718L;
    private final Closure<?> getter, setter;

    GroovyAttribute(final String name,
                    final OpenType<?> type,
                    final String description,
                    final Closure<?> getter,
                    final Closure<?> setter,
                    final AttributeDescriptor descriptor) {
        super(name, type, description, getSpecifier(getter, setter), descriptor);
        this.getter = getter;
        this.setter = setter;
    }

    Object getValue() throws ReflectionException {
        if (getter == null)
            throw JMExceptionUtils.unreadableAttribute(getName(), ReflectionException::new);
        try {
            return getter.call();
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    void setValue(final Object value) throws ReflectionException {
        if (setter == null)
            throw JMExceptionUtils.unwritableAttribute(getName(), ReflectionException::new);
        try {
            setter.call(value);
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    private static AttributeSpecifier getSpecifier(final Closure<?> getter, final Closure<?> setter){
        AttributeSpecifier result = AttributeSpecifier.NOT_ACCESSIBLE;
        if(getter != null)
            result = result.readable(true);
        if(setter != null)
            result = result.writable(true);
        return result;
    }
}
