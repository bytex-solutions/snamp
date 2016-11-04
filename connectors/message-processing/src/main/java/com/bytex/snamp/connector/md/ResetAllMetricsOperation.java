package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;

import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.bytex.snamp.ArrayUtils.emptyArray;

/**
 * Resets all metrics.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ResetAllMetricsOperation extends SpecialOperation<Void, SimpleType<Void>> {
    static final String NAME = "resetAllMetrics";
    private static final long serialVersionUID = -4018417749021221377L;

    ResetAllMetricsOperation(final String name, final OperationDescriptor descriptor) {
        super(name, "Resets all metrics", emptyArray(OpenMBeanParameterInfoSupplier[].class), SimpleType.VOID, ACTION, descriptor);
    }

    @Override
    Void invoke(final AttributeLookup lookup, final Map<String, ?> arguments) throws Exception {
        lookup.forEachAttribute(MetricHolderAttribute.class, MetricHolderAttribute::reset);
        return null;
    }
}
