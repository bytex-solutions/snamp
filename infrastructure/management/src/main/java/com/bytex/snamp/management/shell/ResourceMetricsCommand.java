package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.metrics.*;
import com.bytex.snamp.management.jmx.MetricsAttribute;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.io.PrintWriter;

import static com.bytex.snamp.jmx.MetricsConverter.fromRate;
import static com.google.common.collect.Iterables.getFirst;

/**
 * Provides access to metrics.
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "resource-metrics",
    description = "View runtime metrics associated with managed resources")
@Service
public final class ResourceMetricsCommand extends SnampShellCommand {
    @Argument(index = 0, name = "resourceName", required = false, description = "Name of the managed resource for which metrics should be displayed")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String resourceName = "";

    @Option(name = "-a", aliases = "--attributes", required = false, description = "Show metrics for attributes")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showAttributes;

    @Option(name = "-n", aliases = "--notifications", required = false, description = "Show metrics for notifications")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showNotifications;

    @Option(name = "-o", aliases = "--operations", required = false, description = "Show metrics for operations")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showOperations;

    @Option(name = "-r", aliases = "--reset", required = false, description = "Reset metrics")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean resetMetrics;

    private static void printMetrics(final Rate rate, final PrintWriter output){
        final CompositeData data = fromRate(rate);
        final CompositeType type = data.getCompositeType();
        type.keySet().forEach(itemName -> {
            final String description = type.getDescription(itemName);
            final Object value = data.get(itemName);
            output.format("%s: %s", description, value).println();
        });
    }

    private static void collectMetrics(final AttributeMetrics metrics, final PrintWriter output) {
        if(metrics == null){
            output.println("No metrics for attributes");
            return;
        }
        output.println("Attribute writes:");
        printMetrics(metrics.writes(), output);
        output.println("Attribute reads:");
        printMetrics(metrics.reads(), output);
    }

    private static void collectMetrics(final NotificationMetric metrics, final PrintWriter output) {
        if(metrics == null) {
            output.println("No metrics for notifications");
            return;
        }
        output.println("Notification metrics:");
        printMetrics(metrics.notifications(), output);
    }

    private static void collectMetrics(final OperationMetric metrics, final PrintWriter output) {
        if(metrics == null) {
            output.println("No metrics for operations");
            return;
        }
        output.println("Operation metrics:");
        printMetrics(metrics.invocations(), output);
    }

    private boolean showAll(){
        return !(showAttributes | showNotifications | showOperations);
    }

    private void collectMetrics(final MetricsSupport metrics, final PrintWriter output) {
        if (showAttributes || showAll())
            collectMetrics(getFirst(metrics.getMetrics(AttributeMetrics.class), (AttributeMetrics)null), output);
        if (showNotifications || showAll())
            collectMetrics(getFirst(metrics.getMetrics(NotificationMetric.class), (NotificationMetric)null), output);
        if (showOperations || showAll())
            collectMetrics(getFirst(metrics.getMetrics(OperationMetric.class), (OperationMetric)null), output);
    }

    private static void resetMetrics(final Iterable<? extends Metric> m){
        m.forEach(Metric::reset);
    }

    private void resetMetrics(final MetricsSupport metrics, final PrintWriter output) {
        if (showAttributes || showAll())
            resetMetrics(metrics.getMetrics(AttributeMetrics.class));
        if (showOperations || showAll())
            resetMetrics(metrics.getMetrics(OperationMetric.class));
        if (showNotifications || showAll())
            resetMetrics(metrics.getMetrics(NotificationMetric.class));
        output.println("Metrics reset");
    }

    @Override
    public void execute(final PrintWriter output) throws InstanceNotFoundException {
        MetricsAttribute.getMetrics(resourceName, getBundleContext())
                .ifPresent(metrics -> {
                    if (resetMetrics)
                        resetMetrics(metrics, output);
                    else
                        collectMetrics(metrics, output);
                });
    }
}
