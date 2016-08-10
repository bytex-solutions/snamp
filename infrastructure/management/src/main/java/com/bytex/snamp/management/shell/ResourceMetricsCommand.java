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

    private static void collectMetrics(final AttributeMetrics metrics, final StringBuilder output) {
        if(metrics == null){
            appendln(output, "No metrics for attributes");
            return;
        }
        appendln(output, "Total number of writes: %s", metrics.getNumberOfWrites());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of writes(%s): %s", interval, metrics.getNumberOfWrites(interval));

        appendln(output, "Total number of reads: %s", metrics.getNumberOfReads());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of reads(%s): %s", interval, metrics.getNumberOfReads(interval));
    }

    private static void collectMetrics(final NotificationMetrics metrics, final StringBuilder output) {
        if(metrics == null) {
            appendln(output, "No metrics for notifications");
            return;
        }
        appendln(output, "Total number of emitted notifications: %s", metrics.getNumberOfEmitted());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of emitted notifications(%s %s): %s", "last", interval.name().toLowerCase(), metrics.getNumberOfEmitted(interval));
    }

    private static void collectMetrics(final OperationMetrics metrics, final StringBuilder output) {
        if(metrics == null) {
            appendln(output, "No metrics for operations");
            return;
        }
        appendln(output, "Total number of invocations: %s", metrics.getNumberOfInvocations());
        for (final MetricsInterval interval : MetricsInterval.values())
            appendln(output, "Number of invocations(%s %s): %s", "last", interval.name().toLowerCase(), metrics.getNumberOfInvocations(interval));
    }

    private boolean showAll(){
        return !(showAttributes | showNotifications | showOperations);
    }

    private  CharSequence collectMetrics(final MetricsReader metrics) {
        final StringBuilder result = new StringBuilder();
        if (showAttributes | showAll())
            collectMetrics(metrics.queryObject(AttributeMetrics.class), result);
        if (showNotifications | showAll())
            collectMetrics(metrics.queryObject(NotificationMetrics.class), result);
        if (showOperations | showAll())
            collectMetrics(metrics.queryObject(OperationMetrics.class), result);
        return result;
    }

    private static void resetMetrics(final Metrics m){
        if(m != null)
            m.reset();
    }

    private CharSequence resetMetrics(final MetricsReader metrics) {
        if (showAttributes | showAll())
            resetMetrics(metrics.queryObject(AttributeMetrics.class));
        if (showOperations | showAll())
            resetMetrics(metrics.queryObject(OperationMetrics.class));
        if (showNotifications | showAll())
            resetMetrics(metrics.queryObject(NotificationMetrics.class));
        return "Metrics reset";
    }

    @Override
    protected CharSequence doExecute() throws InstanceNotFoundException {
        final MetricsReader metrics = MetricsAttribute.getMetrics(resourceName, bundleContext);
        if (metrics == null) return "Metrics are not supported";
        return resetMetrics ? resetMetrics(metrics) : collectMetrics(metrics);
    }
}
