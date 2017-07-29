package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.io.IOUtils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;

/**
 * Configures attribute of the managed resource.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
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
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean isURL;

    @Option(name = "-l", aliases = "--language", description = "Script language")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String language;

    @Option(name = "-s", aliases = "--script", description = "Path to file with script or absolute URL (if -u was set to true)")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String script;

    private boolean processHealthCheckTrigger(final ScriptletConfiguration healthCheckTrigger, final PrintWriter output) throws IOException {
        if (del)
            ScriptletConfiguration.fillByDefault(healthCheckTrigger);
        else {
            healthCheckTrigger.getParameters().putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(healthCheckTrigger.getParameters()::remove);
            //setup language
            if (language != null)
                healthCheckTrigger.setLanguage(language);
            //setup script
            healthCheckTrigger.setURL(isURL);
            if (script != null)
                healthCheckTrigger.setScript(isURL ? script : IOUtils.contentAsString(new URL(script)));
        }
        output.println("Supervisor is modified successfully");
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final PrintWriter output) throws IOException {
        if (supervisors.containsKey(groupName))
            return processHealthCheckTrigger(supervisors.get(groupName).getHealthCheckConfig().getTrigger(), output);
        else {
            output.println("Supervisor doesn't exist");
            return false;
        }
    }
}
