package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import javax.management.MBeanException;
import javax.management.openmbean.SimpleType;
import java.io.Serializable;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class Gauge64ExtractionAttribute<T extends Serializable> extends UnaryFunctionAttribute<T> {
    private static final long serialVersionUID = -8981662276608894554L;

    Gauge64ExtractionAttribute(final String name,
                               final String sourceAttribute,
                               final SimpleType<T> type,
                               final String description,
                               final AttributeDescriptor descriptor) {
        super(name, sourceAttribute, type, description, descriptor);
    }

    protected abstract T getValue(final MetricHolderAttribute<?> metric) throws Exception;

    @Override
    protected final T getValue(final AttributeAccessor operand) throws Exception {
        if(operand.getMetadata() instanceof MetricHolderAttribute<?>)
            return getValue((MetricHolderAttribute<?>) operand.getMetadata());
        else
            throw new MBeanException(new IllegalStateException("Metric attribute expected but found " + operand.getName()));
    }
}
