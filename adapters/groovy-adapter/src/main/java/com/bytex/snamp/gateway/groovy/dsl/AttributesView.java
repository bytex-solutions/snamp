package com.bytex.snamp.gateway.groovy.dsl;

import com.bytex.snamp.SpecialUse;

import javax.management.*;
import java.util.Collection;
import java.util.Set;

/**
 * Provides access to attributes.
 */
interface AttributesView {
    Set<String> getAttributes(final String resourceName);

    Collection<MBeanAttributeInfo> getAttributesMetadata(final String resourceName);

    @SpecialUse
    Object getAttributeValue(final String resourceName, final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException;

    @SpecialUse
    void setAttributeValue(final String resourceName, final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException;

}
