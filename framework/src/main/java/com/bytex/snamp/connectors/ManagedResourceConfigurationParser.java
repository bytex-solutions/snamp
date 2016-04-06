package com.bytex.snamp.connectors;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.core.ServiceHolder;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.SMART_MODE_KEY;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.THREAD_POOL_KEY;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.getProperty;

/**
 * Provides parser of connector-related configuration parameters.
 * <p>
 *     Derived class should be placed in the same bundle where connector located.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public abstract class ManagedResourceConfigurationParser {
    /**
     * Default value of {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#SMART_MODE_KEY}
     * configuration property.
     */
    public static final boolean DEFAULT_SMART_MODE_VALUE = false;

    /**
     * Default value of {@link com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration#THREAD_POOL_KEY}
     * configuration property.
     */
    public static final String DEFAULT_THREAD_POOL_VALUE = ThreadPoolRepository.DEFAULT_POOL;

    /**
     * Initializes a new configuration parser.
     */
    protected ManagedResourceConfigurationParser(){

    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    /**
     * Determines whether SmartMode should be enabled for configured connector.
     * @param parameters Configuration parameters of the managed resource. Cannot be {@literal null}.
     * @return {@literal true} if SmartMode={@literal true} in the specified configuration parameters.
     * @see #DEFAULT_SMART_MODE_VALUE
     */
    public final boolean isSmartModeEnabled(final Map<String, ?> parameters) {
        if(parameters.containsKey(SMART_MODE_KEY)){
            final Object smartMode = parameters.get(SMART_MODE_KEY);
            return Objects.equals(smartMode, Boolean.TRUE) || Objects.equals(smartMode, Boolean.TRUE.toString());
        }
        else return DEFAULT_SMART_MODE_VALUE;
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
