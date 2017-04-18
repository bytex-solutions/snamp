package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;
import org.osgi.framework.BundleContext;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.bytex.snamp.jmx.OpenMBean.OpenOperation;

/**
 * Resets metrics.
 */
final class ResetMetricsOperation extends OpenOperation<Void, SimpleType<Void>> {
    private static final OpenMBeanParameterInfoSupplier<String> RESOURCE_NAME_PARAM = new OpenMBeanParameterInfoSupplier<>("resourceName",
            "Name of the resource connector with metrics to reset",
            SimpleType.STRING);

    ResetMetricsOperation(){
        super("resetMetrics", SimpleType.VOID, RESOURCE_NAME_PARAM);
    }

    private static void invoke(final String resourceName, final BundleContext context) throws InstanceNotFoundException {
        MetricsAttribute.getMetrics(resourceName, context).ifPresent(MetricsSupport::reset);
    }

    @Override
    public Void invoke(final Map<String, ?> arguments) throws InstanceNotFoundException {
        invoke(RESOURCE_NAME_PARAM.getArgument(arguments), Utils.getBundleContextOfObject(this));
        return null;
    }
}
