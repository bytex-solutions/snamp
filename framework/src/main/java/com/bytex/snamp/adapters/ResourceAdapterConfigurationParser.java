package com.bytex.snamp.adapters;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.core.ServiceHolder;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.internal.Utils.*;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public abstract class ResourceAdapterConfigurationParser {
    /**
     * Default value of {@link com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration#THREAD_POOL_KEY}
     * configuration property.
     */
    public static final String DEFAULT_THREAD_POOL_VALUE = ThreadPoolRepository.DEFAULT_POOL;

    protected ResourceAdapterConfigurationParser(){

    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    /**
     * Extracts a reference to thread pool using its name defined in configuration parameters.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return Thread pool specified in configuration parameters; or {@literal null} if {@link ThreadPoolRepository} service was not registered.
     */
    public final ExecutorService getThreadPool(final Map<String, ?> parameters) {
        final String poolName = getProperty(parameters, THREAD_POOL_KEY, String.class, DEFAULT_THREAD_POOL_VALUE);
        final ServiceHolder<ThreadPoolRepository> repository = ServiceHolder.tryCreate(getBundleContext(), ThreadPoolRepository.class);
        if (repository != null)
            try {
                return repository.get().getThreadPool(poolName, true);
            } finally {
                repository.release(getBundleContext());
            }
        else return null;
    }
}
