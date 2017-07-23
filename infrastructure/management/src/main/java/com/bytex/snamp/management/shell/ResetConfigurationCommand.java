package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

/**
 * Resets SNAMP configuration.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "reset-config",
    description = "Reset configuration to empty")
@Service
public final class ResetConfigurationCommand extends SnampShellCommand  {
    @Override
    public void execute(final PrintWriter output) throws IOException {
        final Optional<ServiceHolder<ConfigurationManager>> adminRef = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (adminRef.isPresent()) {
            final ServiceHolder<ConfigurationManager> admin = adminRef.get();
            try {
                admin.get().processConfiguration(config -> {
                    config.clear();
                    return true;
                });
            } finally {
                admin.release(getBundleContext());
            }
        } else
            throw new IOException("Configuration storage is not available");
    }
}
