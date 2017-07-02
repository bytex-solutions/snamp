package com.bytex.snamp.management.shell;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceTemplate;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Arrays;

/**
 * Configures attribute of the managed resource.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
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

    @SpecialUse(SpecialUse.Case.REFLECTION)
    @Option(name = "-o", aliases = "--override", description = "Override configuration of attribute declared by group")
    private boolean override;

    private boolean processAttributes(final EntityMap<? extends AttributeConfiguration> attributes, final PrintWriter output){
        if(del)
            attributes.remove(attributeName);
        else {
            final AttributeConfiguration attribute = attributes.getOrAdd(attributeName);
            if (readWriteTimeout > INFINITE_TIMEOUT)
                attribute.setReadWriteTimeout(Duration.ofMillis(readWriteTimeout));
            attribute.putAll(StringKeyValue.parse(parameters));
            attribute.setOverridden(override);
            Arrays.stream(parametersToDelete).forEach(attribute::remove);
        }
        output.println("Attribute configured successfully");
        return true;
    }

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceTemplate> configuration, final PrintWriter output) {
        if (configuration.containsKey(name))
            return processAttributes(configuration.get(name).getAttributes(), output);
        else {
            output.println("Resource doesn't exist");
            return false;
        }
    }

    @Nonnull
    @Override
    public EntityMap<? extends ManagedResourceTemplate> apply(final @Nonnull AgentConfiguration owner) {
        return useGroup ? owner.getResourceGroups() : owner.getResources();
    }
}
