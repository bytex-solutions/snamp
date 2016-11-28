package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.jmx.JMExceptionUtils;
import groovy.lang.Closure;

import javax.management.InvalidAttributeValueException;
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
                    final boolean isIs,
                    final AttributeDescriptor descriptor) {
        super(name, type, description, getSpecifier(getter, setter, isIs), descriptor);
        this.getter = getter;
        this.setter = setter;
    }

    private void checkType(final Object value) throws InvalidAttributeValueException {
        if (!getOpenType().isValue(value))
            throw new InvalidAttributeValueException(String.format("Unable cast '%s' to '%s'", value, getOpenType()));
    }

    Object getValue() throws ReflectionException, InvalidAttributeValueException {
        if (getter == null)
            throw JMExceptionUtils.unreadableAttribute(getName(), ReflectionException::new);
        final Object value;
        try {
            value = getter.call();
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
        checkType(value);
        return value;
    }

    void setValue(final Object value) throws ReflectionException, InvalidAttributeValueException {
        if (setter == null)
            throw JMExceptionUtils.unwritableAttribute(getName(), ReflectionException::new);
        checkType(value);
        try {
            setter.call(value);
        } catch (final Exception e) {
            throw new ReflectionException(e);
        }
    }

    private static AttributeSpecifier getSpecifier(final Closure<?> getter, final Closure<?> setter, final boolean isIs){
        AttributeSpecifier result = AttributeSpecifier.NOT_ACCESSIBLE.flag(isIs);
        if(getter != null)
            result = result.readable(true);
        if(setter != null)
            result = result.writable(true);
        return result;
    }
}
