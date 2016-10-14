package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.metrics.Metric;

import javax.management.openmbean.CompositeType;

/**
 * Represents a holder for metric.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MetricHolderAttribute<M extends Metric> extends StatefulAttribute {
    private static final long serialVersionUID = 2645456225474793148L;

    interface MetricCheckpoint extends Metric, Checkpoint{

    }

    MetricHolderAttribute(final String name,
                          final CompositeType type,
                          final String description,
                          final AttributeDescriptor descriptor) {
        super(name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
    }

    @Override
    abstract MetricCheckpoint createCheckpoint();

    abstract boolean loadCheckpoint(final MetricCheckpoint checkpoint);

    @Override
    final boolean loadCheckpoint(final Checkpoint state) {
        return state instanceof MetricCheckpoint && loadCheckpoint((MetricCheckpoint)state);
    }
}
