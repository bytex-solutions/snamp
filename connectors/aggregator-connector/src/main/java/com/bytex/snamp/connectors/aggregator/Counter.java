package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.concurrent.LongAccumulator;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.SimpleType;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Represents counter.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class Counter extends UnaryAttributeAggregation<Long> {
    static final String NAME = "counter";
    private static final long serialVersionUID = 4529159977546061535L;
    private static final String DESCRIPTION = "Summarizes value of the attribute during update interval";

    private final LongAccumulator accumulator;

    protected Counter(final String attributeID,
                      final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.LONG, descriptor);
        accumulator = LongAccumulator.adder(0L,
                AggregatorConnectorConfiguration.getTimeIntervalInMillis(descriptor));
    }

    @Override
    protected Long compute(final Object value) throws NumberFormatException {
        return accumulator.update(NumberUtils.toLong(value));
    }

    static AttributeConfiguration getConfiguration(final BundleContext context) {
        final AttributeConfiguration result = createAttributeConfiguration(context);
        result.setAlternativeName(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
