package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.metrics.MetricsReader;
import com.bytex.snamp.connector.metrics.SummaryMetrics;
import com.bytex.snamp.internal.Utils;
import static com.google.common.base.Strings.isNullOrEmpty;
import org.osgi.framework.BundleContext;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.bytex.snamp.jmx.OpenMBean.OpenOperation;

/**
 * Resets metrics.
 */
final class ResetMetricsOperation extends OpenOperation<Void, SimpleType<Void>> {
    private static final TypedParameterInfo<String> RESOURCE_NAME_PARAM = new TypedParameterInfo<>("resourceName",
            "Name of the resource connector with metrics to reset",
            SimpleType.STRING);

    ResetMetricsOperation(){
        super("resetMetrics", SimpleType.VOID, RESOURCE_NAME_PARAM);
    }

    private static void invoke(final String resourceName, final BundleContext context) throws InstanceNotFoundException {
        final MetricsReader metrics = isNullOrEmpty(resourceName) ?
                new SummaryMetrics(context) :
                MetricsAttribute.getMetrics(resourceName, context);
        metrics.resetAll();
    }

    @Override
    public Void invoke(final Map<String, ?> arguments) throws InstanceNotFoundException {
        invoke(RESOURCE_NAME_PARAM.getArgument(arguments), Utils.getBundleContextOfObject(this));
        return null;
    }
}
