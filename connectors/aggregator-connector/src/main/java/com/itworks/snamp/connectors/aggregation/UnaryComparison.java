package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import java.math.BigDecimal;

/**
 * Represents an attribute which compares the foreign attribute with user defined value.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class UnaryComparison extends UnaryAttributeAggregation<Boolean> {
    private final Comparison comparison;
    private final BigDecimal userDefinedValue;

    UnaryComparison(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(descriptor);
        comparison = AggregatorConnectorConfigurationDescriptor.getComparisonType(descriptor);
        userDefinedValue = new BigDecimal(AggregatorConnectorConfigurationDescriptor.getUserDefinedValue(descriptor));
    }

    @Override
    protected Boolean compute(final Object foreignAttributeValue) throws Exception {
        return comparison.compute(NumberUtils.toBigDecimal(foreignAttributeValue), userDefinedValue);
    }
}
