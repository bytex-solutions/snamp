package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

/**
 * Computes percent.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class BinaryPercent extends BinaryAttributeAggregation<Double> {
    static final String NAME = "percent";
    private static final long serialVersionUID = 3128849869609641503L;
    private static final String DESCRIPTION = "Computes percent value: (first / second) * 100";

    protected BinaryPercent(final String attributeID,
                            final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.DOUBLE, descriptor);
    }

    static double compute(final BigDecimal first,
                                  final BigDecimal second){
        return 100F * first.divide(second, 5, BigDecimal.ROUND_CEILING).doubleValue();
    }

    @Override
    protected Double compute(final Object left, final Object right) throws Exception {
        return compute(NumberUtils.toBigDecimal(left), NumberUtils.toBigDecimal(right));
    }

    static SerializableAttributeConfiguration getConfiguration() {
        final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
