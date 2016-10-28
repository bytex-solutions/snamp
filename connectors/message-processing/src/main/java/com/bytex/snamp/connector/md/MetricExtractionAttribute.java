package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;

import javax.management.openmbean.SimpleType;
import java.io.Serializable;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MetricExtractionAttribute<T extends Serializable, M extends MetricHolderAttribute<?>> extends UnaryFunctionAttribute<T> {
    private static final long serialVersionUID = -8981662276608894554L;

    MetricExtractionAttribute(final String name,
                              final String sourceAttribute,
                              final SimpleType<T> type,
                              final String description,
                              final AttributeDescriptor descriptor) {
        super(name, sourceAttribute, type, description, descriptor);
    }

    abstract Class<M> getMetricAttributeType();

    abstract T getValue(final M metric);

    @Override
    protected final T getValue(final AttributeAccessor operand) throws ClassCastException {
        return getValue(getMetricAttributeType().cast(operand.getMetadata()));
    }
}
