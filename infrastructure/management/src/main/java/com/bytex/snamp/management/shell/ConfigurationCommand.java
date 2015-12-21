package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.PersistentConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import com.google.common.collect.ImmutableSet;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.service.cm.ConfigurationAdmin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.FeatureConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class ConfigurationCommand extends OsgiCommandSupport implements SnampShellCommand {

    /**
     * Processes configuration.
     * @param configuration Configuration to process. Cannot be {@literal null}.
     * @param output Output writer.
     * @return {@literal true} to save changes; otherwise, {@literal false}.
     * @throws Exception Unable to process configuration.
     */
    abstract boolean doExecute(final AgentConfiguration configuration, final StringBuilder output) throws Exception;

    @Override
    protected final CharSequence doExecute() throws Exception {
        final ServiceHolder<ConfigurationAdmin> adminRef = new ServiceHolder<>(bundleContext, ConfigurationAdmin.class);
        try {
            final PersistentConfigurationManager configurationManager = new PersistentConfigurationManager(adminRef);
            configurationManager.load();
            final StringBuilder output = new StringBuilder(64);
            if(doExecute(configurationManager.getCurrentConfiguration(), output))
                configurationManager.save();
            return output;
        }
        finally {
            adminRef.release(bundleContext);
        }
    }
    protected static <T extends FeatureConfiguration> Set<? extends Map.Entry<String, ? extends T>> getFeatures(final ManagedResourceConfiguration resource,
                                                                                            final Class<T> featureType) {
        final Map<String, ? extends T> features = resource.getFeatures(featureType);
        return features != null ? features.entrySet() : Collections.<Map.Entry<String, ? extends T>>emptySet();
    }

    protected static <T extends FeatureConfiguration> Set<? extends Map.Entry<String, ? extends T>> getFeatures(final AgentConfiguration config,
                                                                                             final String resourceName,
                                                                                            final Class<T> featureType) {
        return getFeatures(config.getManagedResources().get(resourceName), featureType);
    }
}
