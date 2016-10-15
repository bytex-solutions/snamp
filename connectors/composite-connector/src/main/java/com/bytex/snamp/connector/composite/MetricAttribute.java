package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.metrics.Metric;

import javax.management.Descriptor;
import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class MetricAttribute<M extends Metric> extends AbstractCompositeAttribute {
    private static final long serialVersionUID = -2642294369415157342L;

    MetricAttribute(final String name,
                                                 final String type,
                                                 final String description,
                                                 final boolean isReadable,
                                                 final boolean isWritable,
                                                 final boolean isIs,
                                                 final Descriptor descriptor) {
        super(name, type, description, isReadable, isWritable, isIs, descriptor);
    }

    abstract M getMetric();

    abstract boolean setMetric(final Metric value);

    @Override
    abstract CompositeData getValue(final AttributeSupportProvider provider);

    @Override
    final void setValue(final AttributeSupportProvider provider, final Object value) throws MBeanException {
        throw new MBeanException(new UnsupportedOperationException(String.format("Attribute %s is read-only", getName())));
    }
}
