package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

import java.time.temporal.ChronoUnit;

/**
 * Configures attribute of the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    description = "Configure attribute of the managed resource",
    name = "configure-attribute")
public final class ConfigAttributeCommand extends ConfigurationCommand<ManagedResourceConfiguration> {
    private static final long INFINITE_TIMEOUT = -1;

    @SpecialUse
    @Argument(index = 0, name = "resourceName", required = true, description = "Name of the resource to modify")
    private String resourceName = "";

    @SpecialUse
    @Argument(index = 1, name = "name", required = true, description = "Attribute name")
    private String name = "";

    @SpecialUse
    @Argument(index = 2, name = "readWriteTimeout", required = false, description = "Read/write timeout for attribute, in millis")
    private long readWriteTimeout = INFINITE_TIMEOUT;

    @SpecialUse
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    public ConfigAttributeCommand(){
        super(ManagedResourceConfiguration.class);
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        if (configuration.containsKey(resourceName)) {
            final ManagedResourceConfiguration resource = configuration.get(resourceName);
            final AttributeConfiguration attribute = resource.getFeatures(AttributeConfiguration.class).getOrAdd(name);
            if (readWriteTimeout > INFINITE_TIMEOUT)
                attribute.setReadWriteTimeout(readWriteTimeout, ChronoUnit.MILLIS);
            if (!ArrayUtils.isNullOrEmpty(parameters))
                for (final String param : parameters) {
                    final StringKeyValue pair = StringKeyValue.parse(param);
                    if(pair != null)
                        attribute.getParameters().put(pair.getKey(), pair.getValue());
                }
            output.append("Attribute configured successfully");
            return true;
        } else {
            output.append("Resource doesn't exist");
            return false;
        }
    }
}
