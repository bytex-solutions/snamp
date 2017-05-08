package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;

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

    private boolean processAutoScalingConfig(final SupervisorConfiguration.AutoScalingConfiguration configuration, final PrintWriter output) {
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final PrintWriter output) {
        if (supervisors.containsKey(groupName))
            return processAutoScalingConfig(supervisors.get(groupName).getAutoScalingConfig(), output);
        else {
            output.println("Supervisor doesn't exist");
            return false;
        }
    }
}
