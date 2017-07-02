package com.bytex.snamp.configuration;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.internal.Utils;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.configuration.ThreadPoolConfigurationSupport.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ThreadPoolResolver extends ConfigurationEntityDescriptionProvider {
    /**
     * Extracts a reference to thread pool using its name defined in configuration parameters.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return Thread pool specified in configuration parameters; or {@literal null} if {@link ThreadPoolRepository} service was not registered.
     */
    default ExecutorService parseThreadPool(final Map<String, String> parameters) {
        final String poolName = parameters.getOrDefault(THREAD_POOL_KEY, ThreadPoolRepository.DEFAULT_POOL);
        return ThreadPoolRepository.getThreadPool(Utils.getBundleContextOfObject(this), poolName, true);
    }
}
