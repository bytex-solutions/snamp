package com.bytex.snamp.concurrent;

import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ManagedService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Manages centralized manager of all thread pools used by SNAMP components.
 * <p>
 *     This service provides at least one pre-configured thread pool called default thread pool.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.1
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
     * Obtains thread pool by its name.
     * @param context The context of caller bundle. Cannot be {@literal null}.
     * @param name The name of thread pool.
     * @param useDefaultIfNotExists {@literal true} to return default thread pool if requested thread pool doesn't exist; {@literal false} to return null if requested thread pool doesn't exist.
     * @return Thread pool associated with the specified name.
     */
    static ExecutorService getThreadPool(final BundleContext context, final String name, final boolean useDefaultIfNotExists) {
        if (context == null)
            return useDefaultIfNotExists ? ForkJoinPool.commonPool() : null;
        else
            return ServiceHolder.tryCreate(context, ThreadPoolRepository.class).map(repository -> {
                try {
                    return repository.get().getThreadPool(name, useDefaultIfNotExists);
                } finally {
                    repository.release(context);
                }
            })
                    .orElse(null);
    }

    static ExecutorService getDefaultThreadPool(final BundleContext context){
        return getThreadPool(context, DEFAULT_POOL, true);
    }
}
