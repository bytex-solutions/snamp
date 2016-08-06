package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.IOException;

/**
 * Resets SNAMP configuration.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "reset-config",
    description = "Reset configuration to empty")
public final class ResetConfigurationCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Override
    protected CharSequence doExecute() throws IOException {
        final ServiceHolder<ConfigurationManager> adminRef = ServiceHolder.tryCreate(bundleContext, ConfigurationManager.class);
        if (adminRef != null)
            try {
                final StringBuilder output = new StringBuilder(64);
                adminRef.get().processConfiguration(config -> {
                    config.clear();
                    return true;
                });
                return output;
            } finally {
                adminRef.release(bundleContext);
            }
        else throw new IOException("Configuration storage is not available");
    }
}
