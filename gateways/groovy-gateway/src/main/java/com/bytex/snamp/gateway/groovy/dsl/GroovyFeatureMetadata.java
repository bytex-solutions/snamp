package com.bytex.snamp.gateway.groovy.dsl;

import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;
import java.util.function.Function;

import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents Groovy-compliant wrapper for {@link MBeanFeatureInfo} class and its derivatives.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
class GroovyFeatureMetadata<F extends MBeanFeatureInfo> extends GroovyObjectSupport {
    private final F metadata;

    GroovyFeatureMetadata(final F metadata) {
        this.metadata = Objects.requireNonNull(metadata);
    }

    @Override
    public final Object getProperty(final String property) {
        return getField(metadata.getDescriptor(), property, Function.identity(), () -> InvokerHelper.getProperty(metadata, property));
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
