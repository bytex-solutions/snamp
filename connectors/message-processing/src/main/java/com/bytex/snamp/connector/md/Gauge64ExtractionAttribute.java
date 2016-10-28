package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.io.Serializable;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class Gauge64ExtractionAttribute<T extends Serializable> extends MetricExtractionAttribute<T, Gauge64Attribute> {
    private static final long serialVersionUID = 7911853190086552218L;

    Gauge64ExtractionAttribute(final String name,
                               final String sourceAttribute,
                               final SimpleType<T> type,
                               final String description,
                               final AttributeDescriptor descriptor) {
        super(name, sourceAttribute, type, description, descriptor);
    }

    @Override
    final Class<Gauge64Attribute> getMetricAttributeType() {
        return Gauge64Attribute.class;
    }

    @Override
    abstract T getValue(final Gauge64Attribute metric);
}
