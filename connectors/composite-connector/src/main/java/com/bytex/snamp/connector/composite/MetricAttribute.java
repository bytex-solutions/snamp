package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.AbstractMetric;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class MetricAttribute<M extends AbstractMetric> extends AbstractCompositeAttribute implements DistributedAttribute {
    private static final long serialVersionUID = -2642294369415157342L;
    private volatile M metric;
    private final Predicate<Object> isInstance;

    MetricAttribute(final String name,
                    final CompositeType type,
                    final String description,
                    final AttributeDescriptor descriptor,
                    final Function<? super String, ? extends M> metricFactory) {
        super(name, type.getClassName(), descriptor.getDescription(description), true, false, false, descriptor);
        metric = metricFactory.apply(name);
        assert metric != null;
        this.isInstance = metric.getClass()::isInstance;
    }

    final void updateMetric(final Consumer<M> updater){
        updater.accept(metric);
    }

    abstract CompositeData getValue(final M metric);

    @Override
    public final M takeSnapshot() {
        return metric;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadFromSnapshot(final Serializable state) {
        if (isInstance.test(state))
            metric = (M) state;
    }

    final CompositeData getValue(){
        return getValue(metric);
    }
}
