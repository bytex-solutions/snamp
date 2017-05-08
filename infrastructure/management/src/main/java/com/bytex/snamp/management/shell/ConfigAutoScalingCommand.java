package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;
import java.time.Duration;

/**
 * Configures automatic scaling for group.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "configure-auto-scaling",
        description = "Configure automatic scaling")
@Service
public final class ConfigAutoScalingCommand extends SupervisorConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "groupName", required = true, description = "Name of the group to be controlled by supervisor")
    private String groupName = "";

    @Option(name = "-e", aliases = "--enable", description = "Enable or disable automatic scaling")
    private boolean enable;

    @Option(name = "-d", aliases = "--delete", description = "Delete all scaling policies and disable automatic scaling")
    private boolean del;

    @Option(name = "-s", aliases = "--scaling-size", description = "Scaling size")
    private int scalingSize = -1;

    @Option(name = "-max", aliases = "--max-cluster-size", description = "Upper bound of scaling")
    private int maxClusterSize = -1;

    @Option(name = "-min", aliases = "--min-cluster-size", description = "Lower bound of scaling")
    private int minClusterSize = -1;

    @Option(name = "-c", aliases = "--cooldown", description = "Cooldown time, in millis")
    private int cooldownTime = -1;


    private boolean processAutoScalingConfig(final SupervisorConfiguration.AutoScalingConfiguration configuration, final PrintWriter output) {
        if(del){
            configuration.getPolicies().clear();
            configuration.setMaxClusterSize(Integer.MAX_VALUE);
            configuration.setMinClusterSize(0);
            configuration.setScalingSize(1);
            configuration.setCooldownTime(Duration.ZERO);
            configuration.setScalingSize(1);
            configuration.setEnabled(false);
        } else {
            configuration.setEnabled(enable);
            if(scalingSize > 0)
                configuration.setScalingSize(scalingSize);
            if(maxClusterSize > 0)
                configuration.setMaxClusterSize(maxClusterSize);
            if(minClusterSize >= 0)
                configuration.setMinClusterSize(minClusterSize);
            if(cooldownTime >= 0)
                configuration.setCooldownTime(Duration.ofMillis(cooldownTime));
        }
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final PrintWriter output) {
        if (supervisors.containsKey(groupName))
            return processAutoScalingConfig(supervisors.get(groupName).getAutoScalingConfig(), output);
        else {
            output.print("Supervisor doesn't exist");
            return false;
        }
    }
}
