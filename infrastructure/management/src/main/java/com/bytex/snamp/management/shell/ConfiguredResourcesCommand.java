package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.bytex.snamp.management.ManagementUtils.appendln;

/**
 * Shows list of configured managed resources.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "resources",
    description = "List of configured managed resources")
@Service
public final class ConfiguredResourcesCommand extends ConfigurationCommand<ManagedResourceConfiguration> {

    @Override
    boolean doExecute(final EntityMap<? extends ManagedResourceConfiguration> configuration, final StringBuilder output) {
        for(final Map.Entry<String, ? extends ManagedResourceConfiguration> resource: configuration.entrySet())
            appendln(output, "Resource: %s. Type: %s. Connection string: %s", resource.getKey(),
                    resource.getValue().getType(),
                    resource.getValue().getConnectionString());
        return false;
    }

    @Nonnull
    @Override
    public EntityMap<? extends ManagedResourceConfiguration> apply(@Nonnull final AgentConfiguration owner) {
        return owner.getResources();
    }
}
