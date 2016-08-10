package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Computes percent.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class BinaryPercent extends BinaryAttributeAggregation<Double> {
    static final String NAME = "percent";
    private static final long serialVersionUID = 3128849869609641503L;
    private static final String DESCRIPTION = "Computes percent value: (first / second) * 100";

    BinaryPercent(final String attributeID,
                  final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.DOUBLE, descriptor);
    }

    static double compute(final BigDecimal first,
                                  final BigDecimal second){
        return 100F * first.divide(second, 5, BigDecimal.ROUND_CEILING).doubleValue();
    }

    @Override
    protected Double compute(final Object left, final Object right) {
        return compute(NumberUtils.toBigDecimal(left), NumberUtils.toBigDecimal(right));
    }

    static AttributeConfiguration getConfiguration() {
        final AttributeConfiguration result = createAttributeConfiguration(BinaryPercent.class.getClassLoader());
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
