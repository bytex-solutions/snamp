package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.TabularDataBuilderRowFill;
import com.bytex.snamp.jmx.TabularTypeBuilder;
import com.bytex.snamp.management.SummaryMetrics;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.util.Map;

import static com.bytex.snamp.jmx.OpenMBean.OpenAttribute;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provides detailed information about metrics.
 */
public final class MetricsAttribute extends OpenAttribute<TabularData, TabularType> {
    private static final String RESOURCE_NAME_CELL = "resourceName";
    private static final String METRICS_CELL = "metrics";

    private static final TabularType TYPE = Utils.interfaceStaticInitialize(() -> new TabularTypeBuilder("MetricsTable", "A table of metrics mapped to the resource names")
            .addColumn(RESOURCE_NAME_CELL, "Name of the source of metrics", SimpleType.STRING, true)
            .addColumn(METRICS_CELL, "Set of metrics provided by resource", SummaryMetricsAttribute.TYPE, false)
            .build());

    MetricsAttribute(){
        super("Metrics", TYPE);
    }

    public static MetricsSupport getMetrics(final String resourceName, final BundleContext context) throws InstanceNotFoundException {
        if (isNullOrEmpty(resourceName))
            return new SummaryMetrics(context);
        else {
            final ManagedResourceConnectorClient connector = new ManagedResourceConnectorClient(context, resourceName);
            try {
                return connector.queryObject(MetricsSupport.class);
            } finally {
                connector.release(context);
            }
        }
    }

    @Override
    public TabularData getValue() throws InstanceNotFoundException, OpenDataException {
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final TabularDataBuilderRowFill rows = new TabularDataBuilderRowFill(TYPE);
        for (final Map.Entry<String, ServiceReference<ManagedResourceConnector>> connectorRef : ManagedResourceConnectorClient.getConnectors(context).entrySet()) {
            final ServiceHolder<ManagedResourceConnector> connector = new ServiceHolder<>(context, connectorRef.getValue());
            try {
                final MetricsSupport metrics = connector.get().queryObject(MetricsSupport.class);
                if (metrics == null) continue;
                rows.newRow()
                        .cell(RESOURCE_NAME_CELL, connectorRef.getKey())
                        .cell(METRICS_CELL, SummaryMetricsAttribute.collectMetrics(metrics))
                        .flush();
            } finally {
                connector.release(context);
            }
        }
        return rows.build();
    }
}
