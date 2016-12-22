package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import com.bytex.snamp.configuration.impl.CMThreadPoolParser;
import com.bytex.snamp.core.AbstractFrameworkService;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor.Action;

/**
 * Provides default implementation of {@link ThreadPoolRepository} system service.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ThreadPoolRepositoryImpl extends AbstractFrameworkService implements ThreadPoolRepository, Closeable {
    public static final String PID = CMThreadPoolParser.PID;

    private final AbstractConcurrentResourceAccessor<Map<String, ConfiguredThreadPool>> threadPools =
            new ConcurrentResourceAccessor<>(new HashMap<>());

    private final ExecutorService defaultThreadPool = new ConfiguredThreadPool();
    private final Logger logger = Logger.getLogger("SnampThreadPoolRepository");

    @Override
    public ExecutorService getThreadPool(final String name, final boolean useDefaultIfNotExists) {
        switch (name) {
            case DEFAULT_POOL:
                return defaultThreadPool;
            default:
                return threadPools.read(services -> {
                    if (services.containsKey(name))
                        return services.get(name);
                    else if (useDefaultIfNotExists)
                        return defaultThreadPool;
                    else
                        return null;
                });
        }
    }

    @Override
    @Aggregation(cached = true)
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Iterator<String> iterator() {
        return threadPools.read(services -> ImmutableSet.copyOf(services.keySet()).iterator());
    }

    private static Action<Map<String, ConfiguredThreadPool>, Void, ExceptionPlaceholder> createThreadPoolMerger(final Dictionary<String, ?> properties, final Logger logger){
        return new Action<Map<String, ConfiguredThreadPool>, Void, ExceptionPlaceholder>() {

            private void removeThreadPools(final Map<String, ? extends ExecutorService> threadPools) {
                ImmutableSet.copyOf(threadPools.keySet()).stream().filter(poolName -> properties.get(poolName) == null).forEach(poolName -> {
                    final ExecutorService threadPool = threadPools.remove(poolName);
                    if(threadPool != null)
                        threadPool.shutdown();
                });
            }

            private void addThreadPools(final Map<String, ConfiguredThreadPool> threadPools) {
                final Enumeration<String> keys = properties.keys();
                while (keys.hasMoreElements()) {
                    final String poolName = keys.nextElement();
                    switch (poolName) {
                        case Constants.SERVICE_PID:
                        case Constants.OBJECTCLASS:
                        case DEFAULT_POOL:
                            continue;
                        default:
                            //deserialize configuration
                            final ThreadPoolConfiguration offeredConfig;
                            try {
                                offeredConfig = CMThreadPoolParser.deserialize(poolName, properties, getClass().getClassLoader());
                                assert offeredConfig != null;
                            } catch (final IOException e) {
                                logger.log(Level.SEVERE, "Unable to read thread pool config");
                                continue;
                            }
                            //if exists then compare and merge
                            if (threadPools.containsKey(poolName)) {
                                final ConfiguredThreadPool actualConfig = threadPools.get(poolName);
                                if (!actualConfig.hasConfiguration(offeredConfig))    //replace with a new thread pool
                                    actualConfig.shutdown();
                            }
                            threadPools.put(poolName, new ConfiguredThreadPool(offeredConfig, poolName));
                    }
                }
            }

            @Override
            public Void apply(final Map<String, ConfiguredThreadPool> threadPools) {
                removeThreadPools(threadPools);
                addThreadPools(threadPools);
                return null;
            }
        };
    }

    private static Void destroyThreadPools(final Map<?, ? extends ExecutorService> threadPools){
        threadPools.values().forEach(ExecutorService::shutdown);
        threadPools.clear();
        return null;
    }

    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null) //remove all
            threadPools.write(ThreadPoolRepositoryImpl::destroyThreadPools);
        else    //merge with runtime collection of thread pools
            threadPools.write(createThreadPoolMerger(properties, logger));
    }

    @Override
    public void close() {
        defaultThreadPool.shutdown();
        threadPools.write(ThreadPoolRepositoryImpl::destroyThreadPools);
    }

    @Override
    public void forEach(final Consumer<? super String> action) {
        threadPools.read(pools -> {
            pools.keySet().forEach(action);
            return null;
        });
    }
}
