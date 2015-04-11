package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.DynamicMBean;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class UnaryAttributeAggregation<T> extends AbstractAttributeAggregation<T> {
    private static final long serialVersionUID = 8188453369203515353L;
    private final String foreignAttribute;

    protected UnaryAttributeAggregation(final String attributeID,
                                        final String description,
                                        final OpenType<T> attributeType,
                                        final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(attributeID, description, attributeType, descriptor);
        foreignAttribute = AggregatorConnectorConfigurationDescriptor.getForeignAttributeName(descriptor);
    }

    public final String getOperandAttribute(){
        return foreignAttribute;
    }

    protected abstract T compute(final Object foreignAttributeValue) throws Exception;

    @Override
    protected final T compute(final DynamicMBean attributeSupport) throws Exception {
        return compute(attributeSupport.getAttribute(getOperandAttribute()));
    }
}
