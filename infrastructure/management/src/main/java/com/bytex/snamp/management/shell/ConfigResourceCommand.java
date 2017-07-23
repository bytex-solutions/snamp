package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.google.common.collect.ImmutableSet;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;
import java.util.Arrays;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Configures managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "configure-resource",
    description = "Configure managed resource")
@Service
public final class ConfigResourceCommand extends ManagedResourceConfigurationCommand {
    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the resource")
    private String resourceName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 1, name = "connectionType", required = false, description = "Name of the connector")
    private String connectionType = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 2, name = "connectionString", required = false, description = "Connection string used for connection with managed resource")
    private String connectionString = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete resource")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-p", aliases = {"-param, --parameter"}, required = false, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-o", aliases = "--override", multiValued = true, description = "Override configuration properties declared by group")
    private String[] overriddenProperties;

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> resources, final PrintWriter output) {
        if (del)
            resources.remove(resourceName);
        else {
            final ManagedResourceConfiguration resource = resources.getOrAdd(resourceName);
            //setup connection type
            if (!isNullOrEmpty(connectionType))
                resource.setType(connectionType);
            //setup connection string
            if (!isNullOrEmpty(connectionString))
                resource.setConnectionString(connectionString);
            //setup parameters
            resource.putAll(StringKeyValue.parse(parameters));
            Arrays.stream(parametersToDelete).forEach(resource::remove);
            //override properties
            if(overriddenProperties != null)
                resource.overrideProperties(ImmutableSet.copyOf(overriddenProperties));
        }
        output.println("Resource configured successfully");
        return true;
    }
}
