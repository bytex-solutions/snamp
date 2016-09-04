package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.metrics.*;
import com.bytex.snamp.management.jmx.MetricsAttribute;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import javax.management.InstanceNotFoundException;

import static com.bytex.snamp.management.shell.Utils.appendln;
import static com.google.common.collect.Iterables.getFirst;

/**
 * Provides access to metrics.
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "resource-metrics",
    description = "View runtime metrics associated with managed resources")
public final class ResourceMetricsCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(index = 0, name = "resourceName", required = false, description = "Name of the managed resource for which metrics should be displayed")
    @SpecialUse
    private String resourceName = "";

    @Option(name = "-a", aliases = "--attributes", required = false, description = "Show metrics for attributes")
    @SpecialUse
    private boolean showAttributes;

    @Option(name = "-n", aliases = "--notifications", required = false, description = "Show metrics for notifications")
    @SpecialUse
    private boolean showNotifications;

    @Option(name = "-o", aliases = "--operations", required = false, description = "Show metrics for operations")
    @SpecialUse
    private boolean showOperations;

    @Option(name = "-r", aliases = "--reset", required = false, description = "Reset metrics")
    @SpecialUse
    private boolean resetMetrics;

    private static void collectMetrics(final AttributeMetric metrics, final StringBuilder output) {
        if(metrics == null){
            appendln(output, "No metrics for attributes");
            return;
        }
        appendln(output, "Total number of writes: %s", metrics.getTotalNumberOfWrites());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of writes(%s): %s", interval, metrics.getLastNumberOfWrites(interval));

        appendln(output, "Total number of reads: %s", metrics.getTotalNumberOfReads());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of reads(%s): %s", interval, metrics.getLastNumberOfReads(interval));
    }

    private static void collectMetrics(final NotificationMetric metrics, final StringBuilder output) {
        if(metrics == null) {
            appendln(output, "No metrics for notifications");
            return;
        }
        appendln(output, "Total number of emitted notifications: %s", metrics.getTotalNumberOfNotifications());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of emitted notifications(%s %s): %s", "last", interval.name().toLowerCase(), metrics.getLastNumberOfEmitted(interval));
    }

    private static void collectMetrics(final OperationMetric metrics, final StringBuilder output) {
        if(metrics == null) {
            appendln(output, "No metrics for operations");
            return;
        }
        appendln(output, "Total number of invocations: %s", metrics.getTotalNumberOfInvocations());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of invocations(%s %s): %s", "last", interval.name().toLowerCase(), metrics.getLastNumberOfInvocations(interval));
    }

    private boolean showAll(){
        return !(showAttributes | showNotifications | showOperations);
    }

    private  CharSequence collectMetrics(final MetricsSupport metrics) {
        final StringBuilder result = new StringBuilder();
        if (showAttributes | showAll())
            collectMetrics(getFirst(metrics.getMetrics(AttributeMetric.class), (AttributeMetric)null), result);
        if (showNotifications | showAll())
            collectMetrics(getFirst(metrics.getMetrics(NotificationMetric.class), (NotificationMetric)null), result);
        if (showOperations | showAll())
            collectMetrics(getFirst(metrics.getMetrics(OperationMetric.class), (OperationMetric)null), result);
        return result;
    }

    private static void resetMetrics(final Iterable<? extends Metric> m){
        m.forEach(Metric::reset);
    }

    private CharSequence resetMetrics(final MetricsSupport metrics) {
        if (showAttributes | showAll())
            resetMetrics(metrics.getMetrics(AttributeMetric.class));
        if (showOperations | showAll())
            resetMetrics(metrics.getMetrics(OperationMetric.class));
        if (showNotifications | showAll())
            resetMetrics(metrics.getMetrics(NotificationMetric.class));
        return "Metrics reset";
    }

    @Override
    protected CharSequence doExecute() throws InstanceNotFoundException {
        final MetricsSupport metrics = MetricsAttribute.getMetrics(resourceName, bundleContext);
        if (metrics == null) return "Metrics are not supported";
        return resetMetrics ? resetMetrics(metrics) : collectMetrics(metrics);
    }
}
