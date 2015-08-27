package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.connectors.attributes.AbstractAttributeSupport;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.InvalidAttributeValueException;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents abstract support of MDA attributes.
 */
public abstract class MDAAttributeSupport<M extends MdaAttributeAccessor> extends AbstractAttributeSupport<M> {
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

    protected MDAAttributeSupport(final String resourceName,
                                  final Class<M> attributeMetadataType,
                                  final long expirationTime,
                                  final SimpleTimer lwa,
                                  final Logger logger) {
        super(resourceName, attributeMetadataType);
        this.lastWriteAccess = Objects.requireNonNull(lwa);
        this.expirationTime = expirationTime;
        this.logger = Objects.requireNonNull(logger);
        //try to discover hazelcast
        storage = createStorage(resourceName, Utils.getBundleContextByObject(this));
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
                                                               final BundleContext context){
        final ServiceReference<HazelcastInstance> hazelcast = context.getServiceReference(HazelcastInstance.class);
        if(hazelcast == null) //local storage
            return Maps.newConcurrentMap();
        else {
            final ServiceHolder<HazelcastInstance> holder = new ServiceHolder<>(context, hazelcast);
            try{
                return holder.get().getMap(resourceName);
            }
            finally {
                holder.release(context);
            }
        }
    }
}
