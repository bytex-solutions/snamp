package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
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
