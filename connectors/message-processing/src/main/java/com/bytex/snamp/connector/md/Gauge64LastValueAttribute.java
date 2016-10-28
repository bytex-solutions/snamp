package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RatedGauge64;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class Gauge64LastValueAttribute extends Gauge64ExtractionAttribute<Long> {
    private static final long serialVersionUID = 2779884306474472794L;
    private static final String DESCRIPTION = "Extracts last value accepted by Gauge64";

    Gauge64LastValueAttribute(final String name,
                              final String sourceAttribute,
                              final AttributeDescriptor descriptor) {
        super(name, sourceAttribute, SimpleType.LONG, DESCRIPTION, descriptor);
    }

    @Override
    Long getValue(final Gauge64Attribute metric) {
        return metric.extractAsLong(RatedGauge64::getLastValue);
    }
}
