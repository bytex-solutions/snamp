package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ThreadPoolConfig;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.core.AbstractFrameworkService;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor.ConsistentAction;

/**
 * Provides default implementation of {@link ThreadPoolRepository} system service.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ThreadPoolRepositoryImpl extends AbstractFrameworkService implements ThreadPoolRepository, Closeable {
    public static final String PID = "com.bytex.snamp.concurrency.threadPools";

    private final ConcurrentResourceAccessor<Map<String, ExecutorService>> services =
            new ConcurrentResourceAccessor<>(new HashMap<>());

    private final Logger logger = Logger.getLogger("SnampThreadPoolRepository");
    private final DefaultThreadPool defaultPool = new DefaultThreadPool();
    private final ConfigurationAdmin configAdmin;

    public ThreadPoolRepositoryImpl(final ConfigurationAdmin configAdmin){
        this.configAdmin = Objects.requireNonNull(configAdmin);
    }

    @Override
    public ExecutorService getThreadPool(final String name, final boolean useDefaultIfNotExists) {
        switch (name) {
            case DEFAULT_POOL:
                return defaultPool;
            default:
                return services.read(svcs -> svcs.containsKey(name) ? svcs.get(name): defaultPool);
        }
    }

    @Override
    public ExecutorService registerThreadPool(final String name, final ThreadPoolConfig config) {
        if (DEFAULT_POOL.equals(name))
            throw new IllegalArgumentException("Default thread pool is already registered");
        final ExecutorService executor = services.write( svcs -> {
            if (svcs.containsKey(name))
                throw new IllegalArgumentException(String.format("Thread pool '%s' is already registered", name));
            final ExecutorService executor1 = config.createExecutorService(name);
            svcs.put(name, executor1);
            return executor1;
        });
        //persist configuration
        try {
            final Configuration persistentConfig = configAdmin.getConfiguration(PID);
            Dictionary<String, Object> configuredServices = persistentConfig.getProperties();
            if (configuredServices == null) configuredServices = new Hashtable<>();
            configuredServices.put(name, IOUtils.serialize(config));
            persistentConfig.update(configuredServices);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Unable to persist thread pool configuration", e);
        }
        return executor;
    }

    @Override
    public ThreadPoolConfig getConfiguration(final String name) {
        if (DEFAULT_POOL.equals(name))
            return DefaultThreadPool.getConfig();
        try {
            final Configuration persistentConfig = configAdmin.getConfiguration(PID);
            final Dictionary<String, Object> configuredServices = persistentConfig.getProperties();
            if (configuredServices == null) return null;
            final byte[] serializedConfig = Utils.getProperty(configuredServices, name, byte[].class, (byte[]) null);
            return IOUtils.deserialize(serializedConfig, ThreadPoolConfig.class);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, String.format("Unable to read '%s' thread pool configuration", name), e);
            return null;
        }
    }

    @Override
    public boolean unregisterThreadPool(final String name, final boolean shutdown) {
        if (DEFAULT_POOL.equals(name)) return false;
        final boolean success = services.write(svcs -> {
            final ExecutorService executor = svcs.remove(name);
            if (executor != null) {
                if (shutdown) executor.shutdown();
                return true;
            } else return false;
        });
        if (success) {
            try {
                final Configuration persistentConfig = configAdmin.getConfiguration(PID);
                final Dictionary<String, Object> configuredServices = persistentConfig.getProperties();
                if (configuredServices != null) {
                    configuredServices.remove(name);
                    persistentConfig.update(configuredServices);
                }
            } catch (final IOException e) {
                logger.log(Level.SEVERE, String.format("Unable to persist '%s' thread pool configuration", name), e);
                return false;
            }
        }
        return success;
    }

    @Override
    @Aggregation
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Iterator<String> iterator() {
        return services.read(svcs -> ImmutableSet.copyOf(svcs.keySet()).iterator());
    }

    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {
        if (properties == null) //remove all
            services.write(new ConsistentAction<Map<String, ExecutorService>, Void>() {
                @Override
                public Void invoke(final Map<String, ExecutorService> services) {
                    services.clear();
                    return null;
                }
            });
        else    //merge with runtime collection of thread pools
            services.write(new ConsistentAction<Map<String, ExecutorService>, Void>() {
                private void removeThreadPools(final Map<String, ExecutorService> services) {
                    ImmutableSet.copyOf(services.keySet()).stream().filter(poolName -> properties.get(poolName) == null).forEach(services::remove);
                }

                private void addThreadPools(final Map<String, ExecutorService> services) {
                    final Enumeration<String> keys = properties.keys();
                    while (keys.hasMoreElements()) {
                        final String poolName = keys.nextElement();
                        if (!services.containsKey(poolName)) {
                            final byte[] serializedConfig = Utils.getProperty(properties, poolName, byte[].class, (byte[]) null);
                            final ThreadPoolConfig config;
                            try {
                                config = IOUtils.deserialize(serializedConfig, ThreadPoolConfig.class);
                            } catch (final IOException e) {
                                logger.log(Level.SEVERE, "Unable to read thread pool config");
                                continue;
                            }
                            services.put(poolName, config.createExecutorService(poolName));
                        }
                    }
                }

                @Override
                public Void invoke(final Map<String, ExecutorService> services) {
                    removeThreadPools(services);
                    addThreadPools(services);
                    return null;
                }
            });
    }

    @Override
    public void close() {
        defaultPool.terminate();
        services.write(svcs -> {
            svcs.values().forEach(ExecutorService::shutdown);
            svcs.clear();
            return null;
        });
    }
}
