package com.bytex.snamp.adapters.groovy.dsl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.LazyContainers;
import com.bytex.snamp.concurrent.LazyValue;
import groovy.lang.GroovyObjectSupport;

import javax.management.*;

/**
 * Represents attribute of the managed resource.
 * This class cannot be inherited directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public final class GroovyResourceAttribute extends GroovyObjectSupport {
    private final AttributesView attributes;
    private final String attributeName;
    private final String resourceName;
    private final LazyValue<GroovyFeatureMetadata<MBeanAttributeInfo>> metadataCache;

    GroovyResourceAttribute(final AttributesView attributes,
                            final String resourceName,
                            final String attributeName) {
        this.attributes = attributes;
        this.attributeName = attributeName;
        this.resourceName = resourceName;
        this.metadataCache = LazyContainers.THREAD_SAFE_SOFT_REFERENCED.of(() ->
                attributes.getAttributesMetadata(resourceName).stream()
                        .filter(metadata -> attributeName.equals(metadata.getName()))
                        .map(GroovyFeatureMetadata::new)
                        .findFirst()
                        .orElseGet(() -> null)
        );
    }

    @SpecialUse
    public void setValue(final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        attributes.setAttributeValue(resourceName, attributeName, value);
    }

    @SpecialUse
    public Object getValue() throws MBeanException, AttributeNotFoundException, ReflectionException {
        return attributes.getAttributeValue(resourceName, attributeName);
    }

    @SpecialUse
    public GroovyFeatureMetadata<MBeanAttributeInfo> getMetadata() {
        return metadataCache.get();
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
