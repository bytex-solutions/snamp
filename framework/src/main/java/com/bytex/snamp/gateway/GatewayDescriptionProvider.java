package com.bytex.snamp.gateway;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.internal.Utils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.configuration.GatewayConfiguration.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.2
 */
public interface GatewayDescriptionProvider extends ConfigurationEntityDescriptionProvider {
    /**
     * Default value of {@link GatewayConfiguration#THREAD_POOL_KEY}
     * configuration property.
     */
    String DEFAULT_THREAD_POOL_VALUE = ThreadPoolRepository.DEFAULT_POOL;

    /**
     * Extracts a reference to thread pool using its name defined in configuration parameters.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return Thread pool specified in configuration parameters; or {@literal null} if {@link ThreadPoolRepository} service was not registered.
     */
    default ExecutorService getThreadPool(final Map<String, String> parameters) {
        final String poolName = getValue(parameters, THREAD_POOL_KEY, Function.identity(), () -> DEFAULT_THREAD_POOL_VALUE);
        return ThreadPoolRepository.getThreadPool(Utils.getBundleContextOfObject(this), poolName, true);
    }
}
