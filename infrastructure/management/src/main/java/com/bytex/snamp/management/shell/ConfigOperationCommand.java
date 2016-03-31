package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.OperationConfiguration;

/**
 * Configures operation.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-operation",
    description = "Configure new or existing operation")
public final class ConfigOperationCommand extends ConfigurationCommand {
    private static final long INFINITE_TIMEOUT = -1;

    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "name", required = true, description = "Operation name")
    private String name = "";

    @SpecialUse
    @Argument(index = 2, name = "readWriteTimeout", required = false, description = "Invocation timeout for operation, in millis")
    private long readWriteTimeout = INFINITE_TIMEOUT;

    @SpecialUse
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getManagedResources().containsKey(resourceName)){
            final ManagedResourceConfiguration resource = configuration.getManagedResources().get(resourceName);
            final OperationConfiguration operation = resource.getFeatures(OperationConfiguration.class).getOrAdd(name);
            if(readWriteTimeout > INFINITE_TIMEOUT)
                operation.setInvocationTimeout(TimeSpan.ofMillis(readWriteTimeout));
            if(!ArrayUtils.isNullOrEmpty(parameters))
                for(final String param: parameters) {
                    final StringKeyValue pair = StringKeyValue.parse(param);
                    operation.getParameters().put(pair.getKey(), pair.getValue());
                }
            output.append("Operation configured successfully");
            return true;
        }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
