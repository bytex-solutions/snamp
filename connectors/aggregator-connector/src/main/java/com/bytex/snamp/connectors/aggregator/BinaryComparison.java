package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;

import static com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

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
                              final Object right) throws Exception {
        return comparison.compute(NumberUtils.toBigDecimal(left),
                NumberUtils.toBigDecimal(right));
    }

    static SerializableAttributeConfiguration getConfiguration() {
        final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration();
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        result.getParameters().put(AggregatorConnectorConfiguration.COMPARER_PARAM, "=");
        return result;
    }
}
