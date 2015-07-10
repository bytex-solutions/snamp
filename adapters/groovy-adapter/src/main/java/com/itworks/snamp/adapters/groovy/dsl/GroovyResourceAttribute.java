package com.itworks.snamp.adapters.groovy.dsl;

import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.*;
import java.util.Objects;

/**
 * Represents attribute of the managed resource.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class GroovyResourceAttribute {
    private final AttributesView attributes;
    private final String attributeName;
    private final String resourceName;
    private volatile MBeanAttributeInfo metadata;

    GroovyResourceAttribute(final AttributesView attributes,
                            final String resourceName,
                            final String attributeName){
        this.attributes = attributes;
        this.attributeName = attributeName;
        this.resourceName = resourceName;
    }

    @SpecialUse
    public void setValue(final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        attributes.setAttributeValue(resourceName, attributeName, value);
    }

    @SpecialUse
    public Object getValue() throws MBeanException, AttributeNotFoundException, ReflectionException {
        return attributes.getAttributeValue(resourceName, attributeName);
    }

    private synchronized MBeanAttributeInfo getMetadataSync(){
        if(metadata == null)
            for (final MBeanAttributeInfo metadata : attributes.getAttributesMetadata(resourceName))
                if (Objects.equals(attributeName, metadata.getName()))
                    return this.metadata = metadata;
        return metadata;
    }

    @SpecialUse
    public MBeanAttributeInfo getMetadata() {
        return metadata == null ? getMetadataSync() : metadata;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
