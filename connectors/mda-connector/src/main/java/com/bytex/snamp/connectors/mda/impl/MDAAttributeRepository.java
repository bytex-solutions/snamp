package com.bytex.snamp.connectors.mda.impl;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.mda.AccessTimer;
import com.bytex.snamp.connectors.mda.MDAAttributeAccessor;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class MDAAttributeRepository<M extends MDAAttributeAccessor> extends com.bytex.snamp.connectors.mda.MDAAttributeRepository<M> {
    private final Logger logger;
    private final ConcurrentMap<String, Object> storage;

    protected MDAAttributeRepository(final String resourceName,
                                    final Class<M> featureType,
                                    final TimeSpan expirationTime,
                                    final AccessTimer lastWriteAccess,
                                    final Logger logger) {
        super(resourceName, featureType, expirationTime, lastWriteAccess);
        this.logger = Objects.requireNonNull(logger);
        this.storage = createStorage(Utils.getBundleContextByObject(this), resourceName, logger);
    }

    private static ConcurrentMap<String, Object> createStorage(final BundleContext context,
                                                final String resourceName,
                                                final Logger logger) {
        final ServiceReference<HazelcastInstance> hazelcast = context.getServiceReference(HazelcastInstance.class);
        if (hazelcast == null) { //local storage
            logger.info(String.format("%s MDA Connector uses local in-memory local storage for monitoring data", resourceName));
            return Maps.newConcurrentMap();
        } else {
            final ServiceHolder<HazelcastInstance> holder = new ServiceHolder<>(context, hazelcast);
            try {
                logger.info(String.format("%s MDA Connector uses in-memory data grid (%s) for monitoring data", resourceName, holder.get().getName()));
                return holder.get().getMap(resourceName);
            } finally {
                holder.release(context);
            }
        }
    }

    @Override
    protected final ConcurrentMap<String, Object> getStorage() {
        return storage;
    }

    @Override
    protected final Logger getLogger() {
        return logger;
    }
}
