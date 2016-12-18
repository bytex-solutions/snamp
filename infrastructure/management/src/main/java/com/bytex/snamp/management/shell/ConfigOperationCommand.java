package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.time.Duration;

/**
 * Configures operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "configure-operation",
    description = "Configure new or existing operation")
@Service
public final class ConfigOperationCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    private static final long INFINITE_TIMEOUT = -1;

    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "name", required = true, description = "Operation name")
    private String name = "";

    @SpecialUse
    @Argument(index = 2, name = "invocationTimeout", required = false, description = "Invocation timeout for operation, in millis")
    private long invocationTimeout = INFINITE_TIMEOUT;

    @SpecialUse
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    public ConfigOperationCommand(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if(configuration.containsKey(resourceName)){
            final ManagedResourceConfiguration resource = configuration.get(resourceName);
            final OperationConfiguration operation = resource.getFeatures(OperationConfiguration.class).getOrAdd(name);
            if(invocationTimeout > INFINITE_TIMEOUT)
                operation.setInvocationTimeout(Duration.ofMillis(invocationTimeout));
            if(!ArrayUtils.isNullOrEmpty(parameters))
                for(final String param: parameters) {
                    final StringKeyValue pair = StringKeyValue.parse(param);
                    if (pair != null)
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
