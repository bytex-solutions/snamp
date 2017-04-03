package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

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

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> resources, final StringBuilder output) {
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
            if (!ArrayUtils.isNullOrEmpty(parameters))
                for (final String pair : parameters) {
                    final StringKeyValue keyValue = StringKeyValue.parse(pair);
                    if (keyValue != null)
                        resource.put(keyValue.getKey(), keyValue.getValue());
                }
            Arrays.stream(parametersToDelete).forEach(resource::remove);
        }
        appendln(output, "Resource configured successfully");
        return true;
    }
}
