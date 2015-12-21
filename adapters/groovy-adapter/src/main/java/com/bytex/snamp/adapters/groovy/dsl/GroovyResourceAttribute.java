package com.bytex.snamp.adapters.groovy.dsl;

import com.bytex.snamp.SpecialUse;
import groovy.lang.GroovyObjectSupport;

import javax.management.*;
import java.util.Objects;

/**
 * Represents attribute of the managed resource.
 * This class cannot be inherited directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class GroovyResourceAttribute extends GroovyObjectSupport {
    private final AttributesView attributes;
    private final String attributeName;
    private final String resourceName;
    private volatile GroovyFeatureMetadata<MBeanAttributeInfo> metadataCache;

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

    private synchronized GroovyFeatureMetadata<MBeanAttributeInfo> getMetadataImpl() {
        if (metadataCache == null)
            for (final MBeanAttributeInfo metadata : attributes.getAttributesMetadata(resourceName))
                if (Objects.equals(attributeName, metadata.getName()))
                    return metadataCache = new GroovyFeatureMetadata<>(metadata);
        return metadataCache;
    }

    @SpecialUse
    public GroovyFeatureMetadata<MBeanAttributeInfo> getMetadata() {
        return metadataCache == null ? getMetadataImpl() : metadataCache;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
