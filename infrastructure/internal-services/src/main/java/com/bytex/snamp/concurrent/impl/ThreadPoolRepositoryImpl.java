package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import com.bytex.snamp.core.AbstractFrameworkService;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableSet;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ConfigurationListener;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor.Action;
import static com.bytex.snamp.configuration.ThreadPoolConfiguration.*;

/**
 * Provides default implementation of {@link ThreadPoolRepository} system service.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ThreadPoolRepositoryImpl extends AbstractFrameworkService implements ThreadPoolRepository, Closeable {
    public static final String PID = "com.bytex.snamp.concurrency.threadPools";

    private final ConcurrentResourceAccessor<Map<String, ExecutorService>> threadPools =
            new ConcurrentResourceAccessor<>(new HashMap<>());

    private final ExecutorService defaultThreadPool = createThreadPool(DEFAULT_MIN_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, DEFAULT_KEEP_ALIVE_TIME, INFINITE_QUEUE_SIZE, DEFAULT_THREAD_PRIORITY, "SnampThread");
    private final Logger logger = Logger.getLogger("SnampThreadPoolRepository");

    public static ExecutorService createThreadPool(final int minPoolSize,
                                                          final int maxPoolSize,
                                                          final Duration keepAliveTime,
                                                          final int queueSize,
                                                          final int threadPriority,
                                                          final String threadGroup){
        final GroupedThreadFactory threadFactory = new GroupedThreadFactory(threadGroup, threadPriority);
        final BlockingQueue<Runnable> taskQueue;
        final int corePoolSize;
        if (queueSize == INFINITE_QUEUE_SIZE) {
                /*
                    Using an unbounded queue  will cause new tasks to wait in the queue when all corePoolSize
                    threads are busy. Thus, no more than corePoolSize threads will ever be created.
                 */
            if (maxPoolSize == Integer.MAX_VALUE) {
                taskQueue = new SynchronousQueue<>();
                corePoolSize = minPoolSize;
            } else {
                taskQueue = new LinkedBlockingQueue<>();
                corePoolSize = maxPoolSize;
            }
        } else {
            taskQueue = maxPoolSize == Integer.MAX_VALUE ?
                    new SynchronousQueue<>() :
                    new ArrayBlockingQueue<>(queueSize);
            corePoolSize = minPoolSize;
        }
        return new ThreadPoolExecutor(corePoolSize,
                maxPoolSize,
                keepAliveTime.toMillis(),
                TimeUnit.MILLISECONDS,
                taskQueue,
                threadFactory);
    }

    @Override
    public ExecutorService getThreadPool(final String name, final boolean useDefaultIfNotExists) {
        switch (name) {
            case DEFAULT_POOL:
                return defaultThreadPool;
            default:
                return threadPools.read(services -> services.containsKey(name) ? services.get(name): defaultThreadPool);
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
            threadPools.write(new Action<Map<String, ExecutorService>, Void, ExceptionPlaceholder>() {
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
                                config = IOUtils.deserialize(serializedConfig, ThreadPoolConfig.class, getClass().getClassLoader());
                            } catch (final IOException e) {
                                logger.log(Level.SEVERE, "Unable to read thread pool config");
                                continue;
                            }
                            services.put(poolName, config.createExecutorService(poolName));
                        }
                    }
                }

                @Override
                public Void apply(final Map<String, ExecutorService> services) {
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
            services.values().forEach(ExecutorService::shutdown);
            services.clear();
            return null;
        });
    }
}
