package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.google.common.base.Strings;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Configures attribute of the managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    description = "Configures attribute of the managed resource",
    name = "configure-attribute")
public final class ConfigAttributeCommand extends ConfigurationCommand {
    private static final long INFINITE_TIMEOUT = -1;

    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "userDefinedName", required = true, description = "User-defined name of the attribute")
    private String userDefinedName = "";

    @SpecialUse
    @Argument(index = 2, name = "attributeName", required = false, description = "Resource-specific name of the attribute")
    private String systemName = "";

    @SpecialUse
    @Argument(index = 3, name = "readWriteTimeout", required = false, description = "Read/write timeout for attribute, in millis")
    private long readWriteTimeout = INFINITE_TIMEOUT;

    @SpecialUse
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Override
    boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) {
        if(configuration.getManagedResources().containsKey(resourceName)){
            final ManagedResourceConfiguration resource = configuration.getManagedResources().get(resourceName);
            final AttributeConfiguration attribute;
            if(resource.getElements(AttributeConfiguration.class).containsKey(userDefinedName))
                attribute = resource.getElements(AttributeConfiguration.class).get(userDefinedName);
            else {
                attribute = resource.newElement(AttributeConfiguration.class);
                resource.getElements(AttributeConfiguration.class).put(userDefinedName, attribute);
            }
            if(!Strings.isNullOrEmpty(systemName))
                attribute.setAttributeName(systemName);
            if(readWriteTimeout > INFINITE_TIMEOUT)
                attribute.setReadWriteTimeout(TimeSpan.ofMillis(readWriteTimeout));
            if(!ArrayUtils.isNullOrEmpty(parameters))
                for(final String param: parameters) {
                    final StringKeyValue pair = StringKeyValue.parse(param);
                    attribute.getParameters().put(pair.getKey(), pair.getValue());
                }
            output.append("Attribute configured successfully");
            return true;
        }
        else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
