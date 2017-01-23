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

import static com.bytex.snamp.jmx.MetricsConverter.fromRate;
import static com.bytex.snamp.management.ManagementUtils.appendln;
import static com.google.common.collect.Iterables.getFirst;

/**
 * Provides access to metrics.
 */
@Command(scope = SnampShellCommand.SCOPE,
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

    private static void printMetrics(final Rate rate, final StringBuilder output){
        final CompositeData data = fromRate(rate);
        final CompositeType type = data.getCompositeType();
        type.keySet().forEach(itemName -> {
            final String description = type.getDescription(itemName);
            final Object value = data.get(itemName);
            appendln(output, "%s: %s", description, value);
        });
    }

    private static void collectMetrics(final AttributeMetric metrics, final StringBuilder output) {
        if(metrics == null){
            appendln(output, "No metrics for attributes");
            return;
        }
        appendln(output, "Attribute writes:");
        printMetrics(metrics.writes(), output);
        appendln(output, "Attribute reads:");
        printMetrics(metrics.reads(), output);
    }

    private static void collectMetrics(final NotificationMetric metrics, final StringBuilder output) {
        if(metrics == null) {
            appendln(output, "No metrics for notifications");
            return;
        }
        appendln(output, "Notification metrics:");
        printMetrics(metrics.notifications(), output);
    }

    private static void collectMetrics(final OperationMetric metrics, final StringBuilder output) {
        if(metrics == null) {
            appendln(output, "No metrics for operations");
            return;
        }
        appendln(output, "Operation metrics:");
        printMetrics(metrics.invocations(), output);
    }

    private boolean showAll(){
        return !(showAttributes | showNotifications | showOperations);
    }

    private  CharSequence collectMetrics(final MetricsSupport metrics) {
        final StringBuilder result = new StringBuilder();
        if (showAttributes || showAll())
            collectMetrics(getFirst(metrics.getMetrics(AttributeMetric.class), (AttributeMetric)null), result);
        if (showNotifications || showAll())
            collectMetrics(getFirst(metrics.getMetrics(NotificationMetric.class), (NotificationMetric)null), result);
        if (showOperations || showAll())
            collectMetrics(getFirst(metrics.getMetrics(OperationMetric.class), (OperationMetric)null), result);
        return result;
    }

    private static void resetMetrics(final Iterable<? extends Metric> m){
        m.forEach(Metric::reset);
    }

    private CharSequence resetMetrics(final MetricsSupport metrics) {
        if (showAttributes || showAll())
            resetMetrics(metrics.getMetrics(AttributeMetric.class));
        if (showOperations || showAll())
            resetMetrics(metrics.getMetrics(OperationMetric.class));
        if (showNotifications || showAll())
            resetMetrics(metrics.getMetrics(NotificationMetric.class));
        return "Metrics reset";
    }

    @Override
    public CharSequence execute() throws InstanceNotFoundException {
        final MetricsSupport metrics = MetricsAttribute.getMetrics(resourceName, getBundleContext());
        if (metrics == null) return "Metrics are not supported";
        return resetMetrics ? resetMetrics(metrics) : collectMetrics(metrics);
    }
}
