package com.bytex.snamp.concurrent;

import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ManagedService;

import java.util.concurrent.ExecutorService;

/**
 * Manages centralized manager of all thread pools used by SNAMP components.
 * <p>
 *     This service provides at least one pre-configured thread pool called default thread pool.
 * @author Roman Sakno
 * @since 1.2
 * @version 1.2
 */
public interface ThreadPoolRepository extends SupportService, ManagedService, Iterable<String> {
    /**
     * Name of default executor service.
     */
    String DEFAULT_POOL = "DEFAULT";

    /**
     * Obtains thread pool by its name.
     * @param name The name of thread pool.
     * @param useDefaultIfNotExists {@literal true} to return default thread pool if requested thread pool doesn't exist; {@literal false} to return null if requested thread pool doesn't exist.
     * @return Thread pool associated with the specified name.
     */
    ExecutorService getThreadPool(final String name, final boolean useDefaultIfNotExists);

    /**
     * Register a new thread pool with the specified configuration.
     * @param name The name of registered thread pool.
     * @param config Thread pool configuration.
     * @return Instantiated thread pool.
     * @throws IllegalArgumentException Service with specified name is already registered.
     */
    ExecutorService registerThreadPool(final String name, final ThreadPoolConfig config);

    /**
     * Gets configuration of thread pool.
     * @param name The name of thread pool.
     * @return Private copy of thread pool configuration; or {@literal null}, if it doesn't exist.
     */
    ThreadPoolConfig getConfiguration(final String name);

    /**
     * Unregister thread pool.
     * @param name The name of thread pool to unregister.
     * @param shutdown {@literal true} to shutdown thread pool; {@literal false} to reuse thread pool after de-registration
     * @return {@literal true} if thread pool is unregistered successfully; otherwise, {@literal false}.
     */
    boolean unregisterThreadPool(final String name, final boolean shutdown);

    /**
     * Obtains thread pool by its name.
     * @param context The context of caller bundle. Cannot be {@literal null}.
     * @param name The name of thread pool.
     * @param useDefaultIfNotExists {@literal true} to return default thread pool if requested thread pool doesn't exist; {@literal false} to return null if requested thread pool doesn't exist.
     * @return Thread pool associated with the specified name.
     */
    static ExecutorService getThreadPool(final BundleContext context, final String name, final boolean useDefaultIfNotExists){
        final ServiceHolder<ThreadPoolRepository> repository = ServiceHolder.tryCreate(context, ThreadPoolRepository.class);
        if (repository != null)
            try {
                return repository.get().getThreadPool(name, useDefaultIfNotExists);
            } finally {
                repository.release(context);
            }
        else return null;
    }
}
