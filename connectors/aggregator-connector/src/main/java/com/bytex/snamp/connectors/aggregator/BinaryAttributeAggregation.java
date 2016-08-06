package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.openmbean.OpenType;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
abstract class BinaryAttributeAggregation<T> extends AbstractAttributeAggregation<T> {
    private static final long serialVersionUID = 4607161765989878767L;
    private final String leftOperand;
    private final String rightOperand;

    protected BinaryAttributeAggregation(final String attributeID,
                                         final String description,
                                         final OpenType<T> attributeType,
                                         final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID, description, attributeType, descriptor);
        leftOperand = AggregatorConnectorConfiguration.getFirstForeignAttributeName(descriptor);
        rightOperand = AggregatorConnectorConfiguration.getSecondForeignAttributeName(descriptor);
    }

    protected static void fillParameters(final Map<String, String> parameters){
        parameters.put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        parameters.put(AggregatorConnectorConfiguration.FIRST_FOREIGN_ATTRIBUTE_PARAM, "");
        parameters.put(AggregatorConnectorConfiguration.SECOND_FOREIGN_ATTRIBUTE_PARAM, "");
    }

    public final String getFirstOperandAttribute(){
        return leftOperand;
    }

    public final String getSecondOperandAttribute(){
        return rightOperand;
    }

    protected abstract T compute(final Object left, final Object right);

    @Override
    protected final T compute(final DynamicMBean attributeSupport) throws JMException {
        return compute(attributeSupport.getAttribute(getFirstOperandAttribute()),
                attributeSupport.getAttribute(getSecondOperandAttribute()));
    }
}
