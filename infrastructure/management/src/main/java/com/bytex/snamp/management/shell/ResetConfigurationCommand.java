package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.IOException;

/**
 * Resets SNAMP configuration.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "reset-config",
    description = "Reset configuration to empty")
@Service
public final class ResetConfigurationCommand extends SnampShellCommand  {
    @Override
    public CharSequence execute() throws IOException {
        final ServiceHolder<ConfigurationManager> adminRef = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (adminRef != null)
            try {
                final StringBuilder output = new StringBuilder(64);
                adminRef.get().processConfiguration(config -> {
                    config.clear();
                    return true;
                });
                return output;
            } finally {
                adminRef.release(getBundleContext());
            }
        else throw new IOException("Configuration storage is not available");
    }
}
