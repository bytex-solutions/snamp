package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.FactoryMap;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.*;
import java.util.Arrays;

import static com.bytex.snamp.management.ManagementUtils.appendln;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Configures managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
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
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final StringBuilder output) throws Exception {
        if (del)
            supervisors.remove(groupName);
        else {
            final SupervisorConfiguration supervisor = supervisors.getOrAdd(groupName);
            //setup connection type
            if (!isNullOrEmpty(supervisorType))
                supervisor.setType(supervisorType);
            //setup parameters
            if (!ArrayUtils.isNullOrEmpty(parameters))
                for (final String pair : parameters) {
                    final StringKeyValue keyValue = StringKeyValue.parse(pair);
                    if (keyValue != null)
                        supervisor.put(keyValue.getKey(), keyValue.getValue());
                }
            Arrays.stream(parametersToDelete).forEach(supervisor::remove);
        }
        appendln(output, "Supervisor configured successfully");
        return true;
    }
}
