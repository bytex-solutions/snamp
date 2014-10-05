package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.connectors.ManagedEntityType;

import javax.management.InvalidAttributeValueException;

/**
 * Represents JMX-specific management entity type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JmxManagedEntityType extends ManagedEntityType {
    /**
     * Converts well-known management entity value into JMX-specific value.
     * @param value The value to convert.
     * @return JMX-compliant representation of the input object.
     * @throws javax.management.InvalidAttributeValueException Unable to convert value.
     */
    public Object convertToJmx(final Object value) throws InvalidAttributeValueException;
}
