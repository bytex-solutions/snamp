package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.ServiceHolder;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class ConfigurationCommand<E extends EntityConfiguration> extends SnampShellCommand {
    private final EntityMapResolver<AgentConfiguration, E> resolver;

    ConfigurationCommand(final EntityMapResolver<AgentConfiguration, E> resolver){
        this.resolver = Objects.requireNonNull(resolver);
    }

    abstract boolean doExecute(final EntityMap<? extends E> configuration, final StringBuilder output) throws Exception;

    @Override
    public final CharSequence execute() throws Exception {
        final ServiceHolder<ConfigurationManager> adminRef = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (adminRef != null)
            try {
                final StringBuilder output = new StringBuilder(64);
                adminRef.get().processConfiguration(config -> doExecute(resolver.apply(config), output));
                return output;
            } finally {
                adminRef.release(getBundleContext());
            }
        else throw new IOException("Configuration storage is not available");
    }
}
