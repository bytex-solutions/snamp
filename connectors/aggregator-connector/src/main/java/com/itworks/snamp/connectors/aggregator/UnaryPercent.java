package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class UnaryPercent extends UnaryAttributeAggregation<Double>{

    private static final long serialVersionUID = 3477769347977564924L;
    static final String NAME = "percentFrom";
    private static final String DESCRIPTION = "Computes percent value: (first / value) * 100";
    private final BigDecimal userValue;

    UnaryPercent(final String attributeID,
                           final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(attributeID, DESCRIPTION, SimpleType.DOUBLE, descriptor);
        userValue = new BigDecimal(AggregatorConnectorConfigurationDescriptor.getUserDefinedValue(descriptor));
    }


    @Override
    protected Double compute(final Object foreignAttributeValue) throws Exception {
        return BinaryPercent.compute(NumberUtils.toBigDecimal(foreignAttributeValue),
                userValue);
    }
}
