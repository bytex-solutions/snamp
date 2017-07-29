package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;
import java.util.Arrays;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Configures group supervisor.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
        name = "configure-supervisor",
        description = "Configure supervisor")
@Service
public final class ConfigSupervisorCommand extends SupervisorConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "groupName", required = true, description = "Name of the group to be controlled by supervisor")
    private String groupName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 1, name = "supervisorType", required = false, description = "Supervisor type")
    private String supervisorType = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete supervisor")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-p", aliases = {"-param, --parameter"}, required = false, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final PrintWriter output) throws Exception {
        if (del)
            supervisors.remove(groupName);
        else {
            final SupervisorConfiguration supervisor = supervisors.getOrAdd(groupName);
            //setup connection type
            if (!isNullOrEmpty(supervisorType))
                supervisor.setType(supervisorType);
            //setup parameters
            supervisor.putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(supervisor::remove);
        }
        output.print("Supervisor configured successfully");
        return true;
    }
}
