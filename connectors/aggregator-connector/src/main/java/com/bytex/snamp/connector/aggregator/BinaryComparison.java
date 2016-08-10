package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Compares two foreign attributes
 */
final class BinaryComparison extends BinaryAttributeAggregation<Boolean> {
    private static final long serialVersionUID = -499187369674118840L;
    static final String NAME = "comparison";
    private static final String DESCRIPTION = "Compares values of the two foreign attributes";
    private final Comparison comparison;

    BinaryComparison(final String attributeID,
                     final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID,
                DESCRIPTION,
                SimpleType.BOOLEAN,
                descriptor);
        comparison = AggregatorConnectorConfiguration.getComparisonType(descriptor);
    }

    @Override
    protected Boolean compute(final Object left,
                              final Object right) {
        return comparison.compute(NumberUtils.toBigDecimal(left),
                NumberUtils.toBigDecimal(right));
    }

    static AttributeConfiguration getConfiguration() {
        final AttributeConfiguration result = createAttributeConfiguration(BinaryComparison.class.getClassLoader());
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        result.getParameters().put(AggregatorConnectorConfiguration.COMPARER_PARAM, "=");
        return result;
    }
}
