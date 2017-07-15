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
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;

/**
 * Configures scaling policy
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
        name = "configure-scaling-policy",
        description = "Configure scaling policy")
@Service
public final class ConfigScalingPolicyCommand extends SupervisorConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "groupName", required = true, description = "Name of the group to be controlled by supervisor")
    private String groupName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete one or all policies")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-n", aliases = {"--name"}, description = "Name of the scaling policy")
    private String policyName;

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
    private String policy;

    private boolean processScalingPolicies(final FactoryMap<String, ? extends ScriptletConfiguration> policies, final PrintWriter output) throws IOException {
        if (del) {
            if (policyName != null)
                policies.remove(policyName);
            else
                policies.clear();
            return true;
        } else if (policyName == null) {
            output.println("Policy name is not specified");
            return false;
        } else {
            final ScriptletConfiguration checker = policies.getOrAdd(policyName);
            checker.getParameters().putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(checker.getParameters()::remove);
            //setup language
            if (language != null)
                checker.setLanguage(language);
            //setup script
            checker.setURL(isURL);
            if (policy != null)
                checker.setScript(isURL ? policy : IOUtils.contentAsString(new URL(policy)));
            return true;
        }
    }

    @Override
    boolean doExecute(final EntityMap<? extends SupervisorConfiguration> supervisors, final PrintWriter output) throws Exception {
        if (supervisors.containsKey(groupName))
            return processScalingPolicies(supervisors.get(groupName).getAutoScalingConfig().getPolicies(), output);
        else {
            output.println("Supervisor doesn't exist");
            return false;
        }
    }
}
