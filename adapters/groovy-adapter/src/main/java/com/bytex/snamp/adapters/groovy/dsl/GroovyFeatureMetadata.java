package com.bytex.snamp.adapters.groovy.dsl;

import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;
import static com.bytex.snamp.jmx.DescriptorUtils.*;

/**
 * Represents Groovy-compliant wrapper for {@link MBeanFeatureInfo} class and its derivatives.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
class GroovyFeatureMetadata<F extends MBeanFeatureInfo> extends GroovyObjectSupport {
    private final F metadata;

    GroovyFeatureMetadata(final F metadata) {
        this.metadata = Objects.requireNonNull(metadata);
    }

    @Override
    public final Object getProperty(final String property) {
        if (hasField(metadata.getDescriptor(), property))
            return getField(metadata.getDescriptor(), property, Object.class);
        else return InvokerHelper.getProperty(metadata, property);
    }

    @Override
    public final void setProperty(final String property, final Object newValue) {
        if (hasField(metadata.getDescriptor(), property))
            metadata.getDescriptor().setField(property, newValue);
        else InvokerHelper.setProperty(metadata, property, newValue);
    }

    @Override
    public final Object invokeMethod(final String name, final Object args) {
        return InvokerHelper.invokeMethod(metadata, name, args);
    }
}