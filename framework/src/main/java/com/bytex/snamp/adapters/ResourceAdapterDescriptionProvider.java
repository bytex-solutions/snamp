package com.bytex.snamp.adapters;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.internal.Utils;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.internal.Utils.getProperty;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public interface ResourceAdapterDescriptionProvider extends ConfigurationEntityDescriptionProvider {
    /**
     * Default value of {@link com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration#THREAD_POOL_KEY}
     * configuration property.
     */
    String DEFAULT_THREAD_POOL_VALUE = ThreadPoolRepository.DEFAULT_POOL;


    /**
     * Extracts a reference to thread pool using its name defined in configuration parameters.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return Thread pool specified in configuration parameters; or {@literal null} if {@link ThreadPoolRepository} service was not registered.
     */
    default ExecutorService getThreadPool(final Map<String, ?> parameters) {
        final String poolName = getProperty(parameters, THREAD_POOL_KEY, String.class, DEFAULT_THREAD_POOL_VALUE);
        return ThreadPoolRepository.getThreadPool(Utils.getBundleContextOfObject(this), poolName, true);
    }
}
