package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class UnaryPercent extends UnaryAttributeAggregation<Double>{

    private static final long serialVersionUID = 3477769347977564924L;
    static final String NAME = "percentFrom";
    private static final String DESCRIPTION = "Computes percent value: (first / value) * 100";
    private final BigDecimal userValue;

    UnaryPercent(final String attributeID,
                           final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.DOUBLE, descriptor);
        userValue = new BigDecimal(AggregatorConnectorConfiguration.getUserDefinedValue(descriptor));
    }


    @Override
    protected Double compute(final Object foreignAttributeValue) throws Exception {
        return BinaryPercent.compute(NumberUtils.toBigDecimal(foreignAttributeValue),
                userValue);
    }

    static AttributeConfiguration getConfiguration() {
        final AttributeConfiguration result = createAttributeConfiguration(UnaryPercent.class.getClassLoader());
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        result.getParameters().put(AggregatorConnectorConfiguration.VALUE_PARAM, "1");
        return result;
    }
}
