package com.bytex.snamp.gateway.groovy.dsl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.LazyReference;
import groovy.lang.GroovyObjectSupport;

import javax.management.*;

/**
 * Represents attribute of the managed resource.
 * This class cannot be inherited directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public final class GroovyResourceAttribute extends GroovyObjectSupport {
    private final AttributesView attributes;
    private final String attributeName;
    private final String resourceName;
    private final LazyReference<GroovyFeatureMetadata<MBeanAttributeInfo>> metadataCache;

    GroovyResourceAttribute(final AttributesView attributes,
                            final String resourceName,
                            final String attributeName) {
        this.attributes = attributes;
        this.attributeName = attributeName;
        this.resourceName = resourceName;
        this.metadataCache = LazyReference.strong();
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public void setValue(final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        attributes.setAttributeValue(resourceName, attributeName, value);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public Object getValue() throws MBeanException, AttributeNotFoundException, ReflectionException {
        return attributes.getAttributeValue(resourceName, attributeName);
    }

    private GroovyFeatureMetadata<MBeanAttributeInfo> detectMetadata(){
        return attributes.getAttributesMetadata(resourceName).stream()
                        .filter(metadata -> attributeName.equals(metadata.getName()))
                        .map(GroovyFeatureMetadata::new)
                        .findFirst()
                        .orElse(null);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public GroovyFeatureMetadata<MBeanAttributeInfo> getMetadata() {
        return metadataCache.lazyGet(this, GroovyResourceAttribute::detectMetadata);
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
