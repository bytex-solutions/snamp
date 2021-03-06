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
 * Provides configuration of resource discovery supplied by supervisor
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
        description = "Configure discovery service",
        name = "configure-discovery-service")
@Service
public final class ConfigResourceDiscoveryCommand extends SupervisorConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "groupName", required = true, description = "Name of the group to be controlled by supervisor")
    private String groupName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete discovery configuration")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-t", aliases = {"--connection-string-template"}, description = "Delete health check trigger")
    private String connectionStringTemplate;

    private boolean processDiscoveryConfig(final SupervisorConfiguration.ResourceDiscoveryConfiguration discoveryInfo, final PrintWriter output) {
        if (del)
            discoveryInfo.setConnectionStringTemplate(null);
        else if (connectionStringTemplate != null)
            discoveryInfo.setConnectionStringTemplate(connectionStringTemplate);
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final PrintWriter output) {
        if (supervisors.containsKey(groupName))
            return processDiscoveryConfig(supervisors.get(groupName).getDiscoveryConfig(), output);
        else {
            output.println("Supervisor doesn't exist");
            return false;
        }
    }
}
