package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

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
                           final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.DOUBLE, descriptor);
        userValue = new BigDecimal(AggregatorConnectorConfiguration.getUserDefinedValue(descriptor));
    }


    @Override
    protected Double compute(final Object foreignAttributeValue) throws Exception {
        return BinaryPercent.compute(NumberUtils.toBigDecimal(foreignAttributeValue),
                userValue);
    }

    static SerializableAttributeConfiguration getConfiguration() {
        final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration(NAME);
        fillParameters(result.getParameters());
        result.getParameters().put(AggregatorConnectorConfiguration.VALUE_PARAM, "1");
        return result;
    }
}
