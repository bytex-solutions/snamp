package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceTemplate;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Configures operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
    name = "configure-operation",
    description = "Configure new or existing operation")
@Service
public final class ConfigOperationCommand extends TemplateConfigurationCommand {
    private static final long INFINITE_TIMEOUT = -1;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "name", required = true, description = "Name of the resource or group of resources")
    private String name = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 1, name = "operationName", required = true, description = "Operation name")
    private String operationName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 2, name = "invocationTimeout", required = false, description = "Invocation timeout for operation, in millis")
    private long invocationTimeout = INFINITE_TIMEOUT;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-g", aliases = {"--group"}, description = "Configure group instead of resource")
    private boolean useGroup = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete operation from resource or group")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-o", aliases = "--override", description = "Override configuration of operation declared by group")
    private boolean override;

    private boolean processOperations(final EntityMap<? extends OperationConfiguration> operations, final PrintWriter output) {
        if (del)
            operations.remove(operationName);
        else {
            final OperationConfiguration operation = operations.getOrAdd(operationName);
            operation.putAll(StringKeyValue.parse(parameters));
            operation.setOverridden(override);
            Arrays.stream(parametersToDelete).forEach(operation::remove);
        }
        output.println("Operation configured successfully");
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceTemplate> configuration, final PrintWriter output) {
        if (configuration.containsKey(name))
            return processOperations(configuration.get(name).getOperations(), output);
        else {
            output.println("Resource doesn't exist");
            return false;
        }
    }

    @Nonnull
    @Override
    public EntityMap<? extends ManagedResourceTemplate> apply(@Nonnull final AgentConfiguration owner) {
        return useGroup ? owner.getResourceGroups() : owner.getResources();
    }
}
