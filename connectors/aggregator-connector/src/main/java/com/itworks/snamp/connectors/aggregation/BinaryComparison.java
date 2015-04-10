package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

/**
 * Compares two foreign attributes
 */
final class BinaryComparison extends BinaryAttributeAggregation<Boolean> {
    private final Comparison comparison;

    BinaryComparison(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(descriptor);
        comparison = AggregatorConnectorConfigurationDescriptor.getComparisonType(descriptor);
    }

    @Override
    protected Boolean compute(final Object left,
                              final Object right) throws Exception {
        return comparison.compute(NumberUtils.toBigDecimal(left),
                NumberUtils.toBigDecimal(right));
    }
}
