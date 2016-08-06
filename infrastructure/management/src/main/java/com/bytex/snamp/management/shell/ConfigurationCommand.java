package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.ServiceHolder;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.FeatureConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
abstract class ConfigurationCommand<E extends EntityConfiguration> extends OsgiCommandSupport implements SnampShellCommand {
    private final Class<E> entityType;

    ConfigurationCommand(final Class<E> entityType){
        this.entityType = Objects.requireNonNull(entityType);
    }

    abstract boolean doExecute(final EntityMap<? extends E> configuration, final StringBuilder output) throws Exception;

    @Override
    protected final CharSequence doExecute() throws Exception {
        final ServiceHolder<ConfigurationManager> adminRef = ServiceHolder.tryCreate(bundleContext, ConfigurationManager.class);
        if (adminRef != null)
            try {
                final StringBuilder output = new StringBuilder(64);
                adminRef.get().processConfiguration(config -> doExecute(config.getEntities(entityType), output));
                return output;
            } finally {
                adminRef.release(bundleContext);
            }
        else throw new IOException("Configuration storage is not available");
    }

    static <T extends FeatureConfiguration> Set<? extends Map.Entry<String, ? extends T>> getFeatures(final ManagedResourceConfiguration resource,
                                                                                            final Class<T> featureType) {
        final Map<String, ? extends T> features = resource.getFeatures(featureType);
        return features != null ? features.entrySet() : Collections.emptySet();
    }

    static <T extends FeatureConfiguration> Set<? extends Map.Entry<String, ? extends T>> getFeatures(final AgentConfiguration config,
                                                                                             final String resourceName,
                                                                                            final Class<T> featureType) {
        return getFeatures(config.getEntities(ManagedResourceConfiguration.class).get(resourceName), featureType);
    }
}
