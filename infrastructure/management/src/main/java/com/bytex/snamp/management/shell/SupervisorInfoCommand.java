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

import java.io.PrintWriter;


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

    private static void printScriptletConfig(final ScriptletConfiguration scriptlet, final PrintWriter output) {
        output.format("\tLanguage: %s", scriptlet.getLanguage()).println();
        output.format("\tIs URL? %s", scriptlet.isURL()).println();
        output.format("\tConfiguration parameters:").println();
        printParameters(scriptlet.getParameters(), output);
        output.format("\tScript: %s", scriptlet.getScript()).println();
    }

    private static void printHealthCheckConfig(final SupervisorInfo.HealthCheckInfo healthCheckInfo, final PrintWriter output){
        output.println("Health check trigger:");
        printScriptletConfig(healthCheckInfo.getTrigger(), output);
        output.println("Attribute checkers:");
        healthCheckInfo.getAttributeCheckers().forEach((attributeName, checker) -> {
            output.format("\tChecker for attribute %s", attributeName).println();
            printScriptletConfig(checker, output);
            output.println();
        });
    }

    private static void printDiscoveryConfig(final SupervisorInfo.ResourceDiscoveryInfo discoveryInfo, final PrintWriter output){
        output.format("\tConnection String Template: %s", discoveryInfo.getConnectionStringTemplate()).println();
    }

    private static void printAutoScalingConfig(final SupervisorInfo.AutoScalingInfo scalingInfo, final PrintWriter output) {
        output.format("Enabled: %s", scalingInfo.isEnabled()).println();
        output.format("Maximum cluster size: %s", scalingInfo.getMaxClusterSize()).println();
        output.format("Minimum cluster size: %s", scalingInfo.getMinClusterSize()).println();
        output.format("Scaling size: %s", scalingInfo.getScalingSize()).println();
        output.format("Cooldown time: %s", scalingInfo.getCooldownTime()).println();
        output.format("Scaling policies:").println();
        scalingInfo.getPolicies().forEach((policyName, policy) -> {
            output.format("\tScaling policy %s", policyName).println();
            printScriptletConfig(policy, output);
            output.println();
        });
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> configuration, final PrintWriter output) throws InterruptedException {
        if (configuration.containsKey(groupName)) {
            final SupervisorConfiguration supervisor = configuration.get(groupName);
            output.format("Group name: %s", groupName).println();
            output.format("Supervisor Type: %s", supervisor.getType()).println();
            output.format("Configuration parameters:").println();
            printParameters(supervisor, output);
            checkInterrupted();
            if(showHealthCheck) {
                output.println("==HEALTH CHECK==");
                printHealthCheckConfig(supervisor.getHealthCheckConfig(), output);
                output.println();
            }
            checkInterrupted();
            if(showResourceDiscovery){
                output.println("==RESOURCE DISCOVERY==");
                printDiscoveryConfig(supervisor.getDiscoveryConfig(), output);
                output.println();
            }
            checkInterrupted();
            if(showAutoScaling){
                output.println("==AUTO-SCALING==");
                printAutoScalingConfig(supervisor.getAutoScalingConfig(), output);
            }
        } else
            output.append("Resource doesn't exist");
        return false;
    }
}
