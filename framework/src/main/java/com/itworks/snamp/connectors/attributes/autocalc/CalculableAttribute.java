package com.itworks.snamp.connectors.attributes.autocalc;

import com.itworks.snamp.connectors.attributes.AttributeSupport;

import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

/**
 * Represents automatically calculable attribute.
 * @author Roman Sakno
 * @since 1.0
 */
public interface CalculableAttribute {
    /**
     * Computes value of the attribute.
     * @param attributeRegistry The registry with all managed resource attributes.
     * @return Calculated value.
     * @throws ReflectionException Unable to resolve one or more attributes used to compute this attribute.
     * @throws MBeanException An error in computation algorithm.
     */
    Object getValue(final AttributeSupport attributeRegistry) throws ReflectionException, MBeanException;
}
