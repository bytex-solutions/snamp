package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

/**
 * Represents an attribute which compares the foreign attribute with user defined value.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class UnaryComparison extends UnaryAttributeAggregation<Boolean> {
    private static final long serialVersionUID = -1172592787419948019L;
    static final String NAME = "comparisonWith";
    private static final String DESCRIPTION = "Compares value of the foreign attribute with user-defined number";
    private final Comparison comparison;
    private final BigDecimal userDefinedValue;

    UnaryComparison(final String attributeID,
                    final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID,
                DESCRIPTION,
                SimpleType.BOOLEAN,
                descriptor);
        comparison = AggregatorConnectorConfiguration.getComparisonType(descriptor);
        userDefinedValue = new BigDecimal(AggregatorConnectorConfiguration.getUserDefinedValue(descriptor));
    }

    @Override
    protected Boolean compute(final Object foreignAttributeValue) throws Exception {
        return comparison.compute(NumberUtils.toBigDecimal(foreignAttributeValue), userDefinedValue);
    }
}
