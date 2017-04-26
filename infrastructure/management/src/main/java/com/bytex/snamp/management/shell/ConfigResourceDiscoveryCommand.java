package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Provides configuration of resource discovery supplied by supervisor
 */
@Command(scope = SnampShellCommand.SCOPE,
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

    private boolean processDiscoveryConfig(final SupervisorConfiguration.ResourceDiscoveryConfiguration discoveryInfo, final StringBuilder output) {
        if (del)
            discoveryInfo.setConnectionStringTemplate(null);
        else if (connectionStringTemplate != null)
            discoveryInfo.setConnectionStringTemplate(connectionStringTemplate);
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final StringBuilder output) {
        if (supervisors.containsKey(groupName))
            return processDiscoveryConfig(supervisors.get(groupName).getDiscoveryConfig(), output);
        else {
            appendln(output, "Supervisor doesn't exist");
            return false;
        }
    }
}
