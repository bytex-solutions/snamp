package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MDAAttributeInfo;
import com.bytex.snamp.connectors.mda.MDAAttributeRepository;
import com.bytex.snamp.connectors.mq.JMSExceptionUtils;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.jms.*;
import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JMSAttributeRepository extends MDAAttributeRepository<MDAAttributeInfo> {
    private final Logger logger;
    private final ConcurrentMap<String, Object> storage;
    private final JMSAttributeConverter converter;
    private MessageProducer publisher;
    private Session session;

    JMSAttributeRepository(final String resourceName,
                           final JMSAttributeConverter converter,
                           final Logger logger) {
        super(resourceName, MDAAttributeInfo.class);
        this.logger = Objects.requireNonNull(logger);
        this.storage = createStorage(Utils.getBundleContextByObject(this), resourceName, logger);
        this.converter = Objects.requireNonNull(converter);
    }

    void init(final Session session,
                    final String outputQueue,
                    final boolean isTopicOutput) throws JMSException {
        this.session = session;
        //setup output queue
        if (Strings.isNullOrEmpty(outputQueue)) return;
        final Destination outputDestination = isTopicOutput ?
                session.createTopic(outputQueue) :
                session.createQueue(outputQueue);
        this.publisher = session.createProducer(outputDestination);
    }

    private static ConcurrentMap<String, Object> createStorage(final BundleContext context,
                                                               final String resourceName,
                                                               final Logger logger) {
        final ServiceReference<HazelcastInstance> hazelcast = context.getServiceReference(HazelcastInstance.class);
        if (hazelcast == null) { //local storage
            logger.info(String.format("%s MQ Connector uses local in-memory local storage for monitoring data", resourceName));
            return Maps.newConcurrentMap();
        } else {
            final ServiceHolder<HazelcastInstance> holder = new ServiceHolder<>(context, hazelcast);
            try {
                logger.info(String.format("%s MQ Connector uses in-memory data grid (%s) for monitoring data", resourceName, holder.get().getName()));
                return holder.get().getMap(resourceName);
            } finally {
                holder.release(context);
            }
        }
    }

    void setAttribute(final Message message) throws JMSException{
        final String storageKey = converter.getStorageKey(message);
        if(Strings.isNullOrEmpty(storageKey))
            throw new JMSException("storageKey is not defined");
        OpenType<?> attributeType = getAttributeType(storageKey);
        if (Strings.isNullOrEmpty(storageKey) || attributeType == null) return;
        final Object value;
        try {
            value = converter.deserialize(message, attributeType);
        } catch (final OpenDataException e) {
            throw JMSExceptionUtils.wrap("Incorrect JMX data mapping", e);
        }
        getStorage().put(storageKey, value);
    }

    /**
     * Sets the value of a specific attribute of the managed resource.
     *
     * @param attribute The attribute of to set.
     * @param value     The value of the attribute.
     * @throws Exception                      Internal connector error.
     * @throws InvalidAttributeValueException Incompatible attribute type.
     */
    @Override
    protected void setAttribute(final MDAAttributeInfo attribute, final Object value) throws Exception {
        super.setAttribute(attribute, value);
        if(publisher != null){
            final Message message = converter.serialize(value, session);
            converter.setMessageType(message, SnampMessageType.ATTRIBUTE_CHANGED);
            converter.setStorageKey(message, attribute.getStorageKey());
            publisher.send(message);
        }
    }

    /**
     * Connects attribute with this repository.
     *
     * @param attributeID User-defined identifier of the attribute.
     * @param descriptor  Metadata of the attribute.
     * @return Constructed attribute object.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected MDAAttributeInfo createAttributeMetadata(final String attributeID, final AttributeDescriptor descriptor) {
        final AttributeSpecifier specifier = publisher == null ? AttributeSpecifier.READ_ONLY : AttributeSpecifier.READ_WRITE;
        return new MDAAttributeInfo(attributeID, descriptor.getOpenType(), specifier, descriptor);
    }

    /**
     * Gets storage used to read/write attribute values received from external Agents.
     *
     * @return The storage used to read/write attributes.
     */
    @Override
    protected ConcurrentMap<String, Object> getStorage() {
        return storage;
    }


    @Override
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Removes all attributes from this repository.
     */
    @Override
    public void close() {
        publisher = null;
        session = null;
        super.close();
    }
}
