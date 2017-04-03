package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Arrays;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Configures attribute of the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    description = "Configure attribute of the managed resource",
    name = "configure-attribute")
@Service
public final class ConfigAttributeCommand extends TemplateConfigurationCommand {
    private static final long INFINITE_TIMEOUT = -1;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 0, name = "name", required = true, description = "Name of the resource or group of resources")
    private String name = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 1, name = "attributeName", required = true, description = "Attribute name")
    private String attributeName = "";

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Argument(index = 2, name = "readWriteTimeout", required = false, description = "Read/write timeout for attribute, in millis")
    private long readWriteTimeout = INFINITE_TIMEOUT;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-g", aliases = {"--group"}, description = "Configure group instead of resource")
    private boolean useGroup = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-d", aliases = {"--delete"}, description = "Delete attribute from resource or group")
    private boolean del = false;

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-p", aliases = {"-param", "--parameter"}, multiValued = true, description = "Configuration parameters in the form of key=value")
    private String[] parameters = ArrayUtils.emptyArray(String[].class);

    @Option(name = "-dp", aliases = {"--delete-parameter"}, multiValued = true, description = "Configuration parameters to be deleted")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String[] parametersToDelete = parameters;

    private boolean processAttributes(final EntityMap<? extends AttributeConfiguration> attributes, final StringBuilder output){
        if(del)
            attributes.remove(attributeName);
        else {
            final AttributeConfiguration attribute = attributes.getOrAdd(attributeName);
            if (readWriteTimeout > INFINITE_TIMEOUT)
                attribute.setReadWriteTimeout(Duration.ofMillis(readWriteTimeout));
            if (!ArrayUtils.isNullOrEmpty(parameters))
                for (final String param : parameters) {
                    final StringKeyValue pair = StringKeyValue.parse(param);
                    if (pair != null)
                        attribute.put(pair.getKey(), pair.getValue());
                }
            Arrays.stream(parametersToDelete).forEach(attribute::remove);
        }
        appendln(output, "Attribute configured successfully");
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceTemplate> configuration, final StringBuilder output) {
        if (configuration.containsKey(name))
            return processAttributes(configuration.get(name).getAttributes(), output);
        else {
            appendln(output, "Resource doesn't exist");
            return false;
        }
    }

    @Nonnull
    @Override
    public EntityMap<? extends ManagedResourceTemplate> apply(final @Nonnull AgentConfiguration owner) {
        return useGroup ? owner.getResourceGroups() : owner.getResources();
    }
}
