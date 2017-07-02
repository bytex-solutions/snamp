package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.ServiceHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class ConfigurationCommand<E extends EntityConfiguration> extends SnampShellCommand implements EntityMapResolver<AgentConfiguration, E> {
    abstract boolean doExecute(final EntityMap<? extends E> configuration, final PrintWriter output) throws Exception;

    @Override
    public final void execute(final PrintWriter output) throws Exception {
        final Optional<ServiceHolder<ConfigurationManager>> adminRef = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (adminRef.isPresent()) {
            final ServiceHolder<ConfigurationManager> admin = adminRef.get();
            try {
                admin.get().processConfiguration(config -> doExecute(apply(config), output));
            } finally {
                admin.release(getBundleContext());
            }
        } else
            throw new IOException("Configuration storage is not available");
    }

    static void printParameters(final Map<String, String> feature, final PrintWriter output) {
        feature.forEach((key, value) -> output.format("%s=%s", key, value).println());
    }
}
