package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connectors.metrics.GlobalMetrics;
import com.bytex.snamp.connectors.metrics.MetricsReader;
import com.bytex.snamp.internal.Utils;
import com.google.common.base.Strings;
import org.osgi.framework.BundleContext;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
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
        final MetricsReader metrics = Strings.isNullOrEmpty(resourceName) ?
                new GlobalMetrics(context) :
                MetricsAttribute.getMetrics(resourceName, context);
        metrics.resetAll();
    }

    @Override
    public Void invoke(final Map<String, ?> arguments) throws InstanceNotFoundException {
        invoke(RESOURCE_NAME_PARAM.getArgument(arguments), Utils.getBundleContextOfObject(this));
        return null;
    }
}
