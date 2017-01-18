package com.bytex.snamp.connector;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.internal.Utils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.SMART_MODE_KEY;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.THREAD_POOL_KEY;

/**
 * Provides parser of connector-related configuration parameters.
 * <p>
 *     Derived class should be placed in the same bundle where connector located.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface ManagedResourceDescriptionProvider extends ConfigurationEntityDescriptionProvider {
    /**
     * Default value of {@link ManagedResourceConfiguration#SMART_MODE_KEY}
     * configuration property.
     */
    boolean DEFAULT_SMART_MODE_VALUE = false;

    /**
     * Default value of {@link ManagedResourceConfiguration#THREAD_POOL_KEY}
     * configuration property.
     */
    String DEFAULT_THREAD_POOL_VALUE = ThreadPoolRepository.DEFAULT_POOL;

    /**
     * Determines whether SmartMode should be enabled for configured connector.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return {@literal true} if SmartMode={@literal true} in the specified configuration parameters.
     * @see #DEFAULT_SMART_MODE_VALUE
     */
    default boolean isSmartModeEnabled(final Map<String, String> parameters) {
        return getValue(parameters, SMART_MODE_KEY, Boolean::parseBoolean).orElse(DEFAULT_SMART_MODE_VALUE);
    }

    /**
     * Extracts a reference to thread pool using its name defined in configuration parameters.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return Thread pool specified in configuration parameters; or {@literal null} if {@link ThreadPoolRepository} service was not registered.
     */
    default ExecutorService parseThreadPool(final Map<String, String> parameters) {
        final String poolName = getValue(parameters, THREAD_POOL_KEY, Function.identity()).orElse(DEFAULT_THREAD_POOL_VALUE);
        return ThreadPoolRepository.getThreadPool(Utils.getBundleContextOfObject(this), poolName, true);
    }
}
