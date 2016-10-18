package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Box;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.Metric;
import org.osgi.framework.BundleContext;

import javax.management.MBeanException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.util.Objects;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class MetricAttribute<M extends Metric & Serializable> extends AbstractCompositeAttribute {
    private static final long serialVersionUID = -2642294369415157342L;
    final Box<M> metricStorage;

    MetricAttribute(final String name,
                    final CompositeType type,
                    final String description,
                    final AttributeDescriptor descriptor,
                    final Box<M> metricStorage) {
        super(name, type.getClassName(), descriptor.getDescription(description), true, false, false, descriptor);
        this.metricStorage = Objects.requireNonNull(metricStorage);
    }

    final BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    abstract CompositeData getValue(final M metric);

    abstract M createMetrics();

    @Override
    final CompositeData getValue(final AttributeSupportProvider provider){
        final M metric = metricStorage.setIfAbsent(this::createMetrics);
        return getValue(metric);
    }

    @Override
    final void setValue(final AttributeSupportProvider provider, final Object value) throws MBeanException {
        throw new MBeanException(new UnsupportedOperationException(String.format("Attribute %s is read-only", getName())));
    }
}
