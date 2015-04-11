package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.DynamicMBean;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class BinaryAttributeAggregation<T> extends AbstractAttributeAggregation<T> {
    private static final long serialVersionUID = 4607161765989878767L;
    private final String leftOperand;
    private final String rightOperand;

    protected BinaryAttributeAggregation(final String attributeID,
                                         final String description,
                                         final OpenType<T> attributeType,
                                         final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        super(attributeID, description, attributeType, descriptor);
        leftOperand = AggregatorConnectorConfigurationDescriptor.getFirstForeignAttributeName(descriptor);
        rightOperand = AggregatorConnectorConfigurationDescriptor.getSecondForeignAttributeName(descriptor);
    }

    public final String getFirstOperandAttribute(){
        return leftOperand;
    }

    public final String getSecondOperandAttribute(){
        return rightOperand;
    }

    protected abstract T compute(final Object left, final Object right) throws Exception;

    @Override
    protected final T compute(final DynamicMBean attributeSupport) throws Exception {
        return compute(attributeSupport.getAttribute(getFirstOperandAttribute()),
                attributeSupport.getAttribute(getSecondOperandAttribute()));
    }
}
