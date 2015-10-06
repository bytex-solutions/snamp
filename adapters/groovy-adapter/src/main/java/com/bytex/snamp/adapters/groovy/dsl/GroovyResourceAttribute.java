package com.bytex.snamp.adapters.groovy.dsl;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.SimpleCache;
import com.google.common.base.Supplier;
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
public final class GroovyResourceAttribute extends GroovyObjectSupport implements Supplier<GroovyFeatureMetadata<MBeanAttributeInfo>> {
    private static final class GroovyMetadataCache extends SimpleCache<Supplier<GroovyFeatureMetadata<MBeanAttributeInfo>>, GroovyFeatureMetadata<MBeanAttributeInfo>, ExceptionPlaceholder>{

        @Override
        protected GroovyFeatureMetadata<MBeanAttributeInfo> init(final Supplier<GroovyFeatureMetadata<MBeanAttributeInfo>> input) {
            return input.get();
        }
    }

    private final AttributesView attributes;
    private final String attributeName;
    private final String resourceName;
    private final GroovyMetadataCache metadataCache;

    GroovyResourceAttribute(final AttributesView attributes,
                            final String resourceName,
                            final String attributeName){
        this.attributes = attributes;
        this.attributeName = attributeName;
        this.resourceName = resourceName;
        this.metadataCache = new GroovyMetadataCache();
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
        return metadataCache.get(this);
    }

    @Override
    public GroovyFeatureMetadata<MBeanAttributeInfo> get() {
        for (final MBeanAttributeInfo metadata : attributes.getAttributesMetadata(resourceName))
            if (Objects.equals(attributeName, metadata.getName()))
                return new GroovyFeatureMetadata<>(metadata);
        return null;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
