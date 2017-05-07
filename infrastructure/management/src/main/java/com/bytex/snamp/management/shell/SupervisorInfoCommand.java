package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import static com.bytex.snamp.management.ManagementUtils.appendln;
import static com.bytex.snamp.management.ManagementUtils.newLine;

/**
 * Displays information about supervisor.
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "supervisor",
        description = "Display configuration of the managed resource group")
@Service
public final class SupervisorInfoCommand extends SupervisorConfigurationCommand {
    @Argument(index = 0, name = "groupName", required = true, description = "Name of configured resource group to display")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String groupName = "";

    @Option(name = "-h", aliases = {"--health-check"}, description = "Show health check configuration")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showHealthCheck;

    @Option(name = "-d", aliases = {"--resource-discovery"}, description = "Show resource discovery configuration")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showResourceDiscovery;

    @Option(name = "-s", aliases = "--auto-scaling", description = "Show elasticity management configuration")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean showAutoScaling;

    private static void printScriptletConfig(final ScriptletConfiguration scriptlet, final StringBuilder output) {
        appendln(output, "\tLanguage: %s", scriptlet.getLanguage());
        appendln(output, "\tIs URL? %s", scriptlet.isURL());
        appendln(output, "\tConfiguration parameters:");
        scriptlet.getParameters().forEach((key, value) -> appendln(output, "\t%s = %s", key, value));
        appendln(output, "\tScript: %s", scriptlet.getScript());
    }

    private static void printHealthCheckConfig(final SupervisorInfo.HealthCheckInfo healthCheckInfo, final StringBuilder output){
        appendln(output, "Health check trigger:");
        printScriptletConfig(healthCheckInfo.getTrigger(), output);
        appendln(output, "Attribute checkers:");
        healthCheckInfo.getAttributeCheckers().forEach((attributeName, checker) -> {
            appendln(output, "\tChecker for attribute %s", attributeName);
            printScriptletConfig(checker, output);
            newLine(output);
        });
    }

    private static void printDiscoveryConfig(final SupervisorInfo.ResourceDiscoveryInfo discoveryInfo, final StringBuilder output){
        appendln(output, "\tConnection String Template: %s", discoveryInfo.getConnectionStringTemplate());
    }

    private static void printAutoScalingConfig(final SupervisorInfo.AutoScalingInfo scalingInfo, final StringBuilder output) {
        appendln(output, "Enabled: %s", scalingInfo.isEnabled());
        appendln(output, "Maximum cluster size: %s", scalingInfo.getMaxClusterSize());
        appendln(output, "Minimum cluster size: %s", scalingInfo.getMinClusterSize());
        appendln(output, "Scaling size: %s", scalingInfo.getScalingSize());
        appendln(output, "Cooldown time: %s", scalingInfo.getCooldownTime());
        appendln(output, "Scaling policies:");
        scalingInfo.getPolicies().forEach((policyName, policy) -> {
            appendln(output, "\tScaling policy %s", policyName);
            printScriptletConfig(policy, output);
            newLine(output);
        });
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> configuration, final StringBuilder output) throws InterruptedException {
        if (configuration.containsKey(groupName)) {
            final SupervisorConfiguration supervisor = configuration.get(groupName);
            appendln(output, "Group name: %s", groupName);
            appendln(output, "Supervisor Type: %s", supervisor.getType());
            appendln(output, "Configuration parameters:");
            supervisor.forEach((key, value) -> appendln(output, "%s = %s", key, value));
            checkInterrupted();
            if(showHealthCheck) {
                appendln(output, "==HEALTH CHECK==");
                printHealthCheckConfig(supervisor.getHealthCheckConfig(), output);
                newLine(output);
            }
            checkInterrupted();
            if(showResourceDiscovery){
                appendln(output, "==RESOURCE DISCOVERY==");
                printDiscoveryConfig(supervisor.getDiscoveryConfig(), output);
                newLine(output);
            }
            checkInterrupted();
            if(showAutoScaling){
                appendln(output, "==AUTO-SCALING==");
                printAutoScalingConfig(supervisor.getAutoScalingConfig(), output);
            }
        } else
            output.append("Resource doesn't exist");
        return false;
    }
}
