package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.ExceptionPlaceholder;
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

    private final ConcurrentResourceAccessor<Map<String, ConfiguredThreadPool>> threadPools =
            new ConcurrentResourceAccessor<>(new HashMap<>());

    private final ExecutorService defaultThreadPool = new ConfiguredThreadPool(DefaultThreadPoolConfiguration.getInstance(), "SnampThread");
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

    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null) //remove all
            threadPools.write(services -> {
                services.clear();
                return null;
            });
        else    //merge with runtime collection of thread pools
            threadPools.write(new Action<Map<String, ConfiguredThreadPool>, Void, ExceptionPlaceholder>() {

                private void removeThreadPools(final Map<String, ? extends ExecutorService> services) {
                    ImmutableSet.copyOf(services.keySet()).stream().filter(poolName -> properties.get(poolName) == null).forEach(poolName -> {
                        final ExecutorService threadPool = services.remove(poolName);
                        if(threadPool != null)
                            threadPool.shutdown();
                    });
                }

                private void addThreadPools(final Map<String, ConfiguredThreadPool> services) {
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
                                if (services.containsKey(poolName)) {
                                    final ConfiguredThreadPool actualConfig = services.get(poolName);
                                    if (!actualConfig.equals(offeredConfig))    //replace with a new thread pool
                                        actualConfig.shutdown();
                                }
                                services.put(poolName, new ConfiguredThreadPool(offeredConfig, poolName));
                        }
                    }
                }

                @Override
                public Void apply(final Map<String, ConfiguredThreadPool> services) {
                    removeThreadPools(services);
                    addThreadPools(services);
                    return null;
                }
            });
    }

    @Override
    public void close() {
        defaultThreadPool.shutdown();
        threadPools.write(services -> {
            services.values().forEach(ConfiguredThreadPool::shutdown);
            services.clear();
            return null;
        });
    }

    @Override
    public void forEach(final Consumer<? super String> action) {
        threadPools.read(pools -> {
            pools.keySet().forEach(action);
            return null;
        });
    }
}
