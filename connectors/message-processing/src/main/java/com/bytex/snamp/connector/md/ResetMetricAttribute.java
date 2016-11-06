package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;

import javax.management.openmbean.SimpleType;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ResetMetricAttribute extends SpecialOperation<Boolean, SimpleType<Boolean>> {
    static final String NAME = "reset";
    private static final long serialVersionUID = 6717837598225235528L;
    private static final OpenMBeanParameterInfoSupplier<String> ATTR_NAME_PARAM = new OpenMBeanParameterInfoSupplier<>("attributeName", "Attribute name", SimpleType.STRING);

    ResetMetricAttribute(final String name, final OperationDescriptor descriptor) {
        super(name, "Resets metric", new OpenMBeanParameterInfoSupplier[]{ATTR_NAME_PARAM}, SimpleType.BOOLEAN, ACTION, descriptor);
    }

    @Override
    Boolean invoke(final AttributeLookup lookup, final Map<String, ?> arguments) throws Exception {
        final String attributeName = ATTR_NAME_PARAM.getArgument(arguments);
        return lookup.acceptAttribute(attributeName, MetricHolderAttribute.class, MetricHolderAttribute::reset);
    }
}
