package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.connectors.attributes.AbstractAttributeRepository;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.InvalidAttributeValueException;
import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents abstract support of MDA attributes.
 */
public abstract class MDAAttributeRepository<M extends MdaAttributeAccessor> extends AbstractAttributeRepository<M> implements Closeable {
    private final Logger logger;
    private final long expirationTime;
    /**
     * Provides access to timer that measures time of last write.
     */
    protected final SimpleTimer lastWriteAccess;
    /**
     * Represents storage of attribute values.
     */
    protected final ConcurrentMap<String, Object> storage;

    protected MDAAttributeRepository(final String resourceName,
                                     final Class<M> attributeMetadataType,
                                     final long expirationTime,
                                     final SimpleTimer lwa,
                                     final Logger logger) {
        super(resourceName, attributeMetadataType);
        this.lastWriteAccess = Objects.requireNonNull(lwa);
        this.expirationTime = expirationTime;
        this.logger = Objects.requireNonNull(logger);
        //try to discover hazelcast
        storage = createStorage(resourceName, Utils.getBundleContextByObject(this), logger);
    }

    /**
     * Gets default value of the named storage slot.
     * @param storageName The name of the storage slot.
     * @return Default value of the storage slot.
     */
    protected abstract Object getDefaultValue(final String storageName);

    /**
     * Resets all values in the storage to default.
     */
    public final void reset(){
        for(final String storageName: storage.keySet())
            storage.put(storageName, getDefaultValue(storageName));
        lastWriteAccess.reset();
    }

    @Override
    protected final Object getAttribute(final M metadata) {
        if(lastWriteAccess.checkInterval(expirationTime, TimeUnit.MILLISECONDS) > 0)
            throw new IllegalStateException("Attribute value is too old. Backend component must supply a fresh value");
        else
            return metadata.getValue(storage);
    }

    @Override
    protected final void setAttribute(final M attribute, final Object value) throws InvalidAttributeValueException {
        attribute.setValue(value, storage);
        lastWriteAccess.reset();
    }

    @Override
    protected final void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
        failedToConnectAttribute(logger, Level.WARNING, attributeID, attributeName, e);
    }

    @Override
    protected final void failedToGetAttribute(final String attributeID, final Exception e) {
        failedToGetAttribute(logger, Level.SEVERE, attributeID, e);
    }

    @Override
    protected final void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(logger, Level.SEVERE, attributeID, value, e);
    }

    private static ConcurrentMap<String, Object> createStorage(final String resourceName,
                                                               final BundleContext context,
                                                               final Logger logger){
        final ServiceReference<HazelcastInstance> hazelcast = context.getServiceReference(HazelcastInstance.class);
        if(hazelcast == null) { //local storage
            logger.info(String.format("%s MDA Connector uses local in-memory local storage for monitoring data", resourceName));
            return Maps.newConcurrentMap();
        }
        else {
            final ServiceHolder<HazelcastInstance> holder = new ServiceHolder<>(context, hazelcast);
            try{
                logger.info(String.format("%s MDA Connector uses in-memory data grid (%s) for monitoring data", resourceName, holder.get().getName()));
                return holder.get().getMap(resourceName);
            }
            finally {
                holder.release(context);
            }
        }
    }

    @Override
    public void close() {
        removeAll(true);
    }
}