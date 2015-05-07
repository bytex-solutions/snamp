package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.concurrent.PeakLongAccumulator;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.openmbean.SimpleType;
import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class Peak extends UnaryAttributeAggregation<Long> {
    static final String NAME = "peak";
    private static final long serialVersionUID = 9156690032615261535L;
    private static final String DESCRIPTION = "Detects value at the specified time interval";

    private final PeakLongAccumulator accumulator;

    protected Peak(final String attributeID, final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, DESCRIPTION, SimpleType.LONG, descriptor);
        accumulator = new PeakLongAccumulator(0L, AggregatorConnectorConfiguration.getTimeIntervalInMillis(descriptor));
    }

    @Override
    protected Long compute(final Object value) throws NumberFormatException {
        return accumulator.update(NumberUtils.toLong(value));
    }

    static SerializableAttributeConfiguration getConfiguration() {
        final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration(NAME);
        fillParameters(result.getParameters());
        return result;
    }
}
