package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.util.Arrays;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Configures attribute of the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        description = "Configure health check trigger",
        name = "configure-health-check-trigger")
@Service
public final class ConfigHealthCheckTriggerCommand extends SupervisorConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "groupName", required = true, description = "Name of the group to be controlled by supervisor")
    private String groupName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete health check trigger")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    @Option(name = "-u", aliases = "--url", description = "Treat script path as a reference to script in the form of URL")
    private boolean isURL;

    @Option(name = "-l", aliases = "--language", description = "Script language")
    private String language;

    private boolean processHealthCheckTrigger(final ScriptletConfiguration healthCheckTrigger, final StringBuilder output){
        if(del)
            ScriptletConfiguration.fillByDefault(healthCheckTrigger);
        else {
            healthCheckTrigger.getParameters().putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(healthCheckTrigger.getParameters()::remove);
        }
        appendln(output, "Supervisor is modified successfully");
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final StringBuilder output) throws Exception {
        if (supervisors.containsKey(groupName))
            return processHealthCheckTrigger(supervisors.get(groupName).getHealthCheckConfig().getTrigger(), output);
        else {
            appendln(output, "Supervisor doesn't exist");
            return false;
        }
    }
}
