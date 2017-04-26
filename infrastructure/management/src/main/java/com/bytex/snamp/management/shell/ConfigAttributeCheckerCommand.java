package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.FactoryMap;
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
import java.net.URL;
import java.util.Arrays;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Provides configuration of attribute checkers.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        description = "Configure health check trigger",
        name = "configure-attribute-checker")
@Service
public final class ConfigAttributeCheckerCommand extends SupervisorConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "groupName", required = true, description = "Name of the group to be controlled by supervisor")
    private String groupName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete one or all attribute checkers")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-a", aliases = {"--attribute"}, description = "Name of the attribute to check")
    private String attributeName;

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

    private boolean processAttributeCheckers(final FactoryMap<String, ? extends ScriptletConfiguration> checkers, final StringBuilder output) throws IOException {
        if (del) {
            if (attributeName != null)
                checkers.remove(attributeName);
            else
                checkers.clear();
            return true;
        } else if (attributeName == null) {
            appendln(output, "Attribute name is not specified");
            return false;
        } else {
            final ScriptletConfiguration checker = checkers.getOrAdd(attributeName);
            checker.getParameters().putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(checker.getParameters()::remove);
            //setup language
            if (language != null)
                checker.setLanguage(language);
            //setup script
            checker.setURL(isURL);
            if (script != null)
                checker.setScript(isURL ? script : IOUtils.contentAsString(new URL(script)));
            return true;
        }
    }

    @Override
    boolean doExecute(EntityMap<? extends SupervisorConfiguration> supervisors, final StringBuilder output) throws IOException {
        if (supervisors.containsKey(groupName))
            return processAttributeCheckers(supervisors.get(groupName).getHealthCheckConfig().getAttributeCheckers(), output);
        else {
            appendln(output, "Supervisor doesn't exist");
            return false;
        }
    }
}
