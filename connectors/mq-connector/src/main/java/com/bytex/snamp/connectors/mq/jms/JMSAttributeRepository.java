package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MDAAttributeInfo;
import com.bytex.snamp.connectors.mda.MDAAttributeRepository;
import com.bytex.snamp.connectors.mq.JMSExceptionUtils;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import static com.google.common.base.Strings.isNullOrEmpty;

import javax.jms.*;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class JMSAttributeRepository extends MDAAttributeRepository<MDAAttributeInfo> {
    private final Logger logger;
    private final JMSAttributeConverter converter;
    private MessageProducer publisher;
    private Session session;
    private final ConcurrentMap<String, Object> storageService;

    JMSAttributeRepository(final String resourceName,
                           final JMSAttributeConverter converter,
                           final Logger logger) {
        super(resourceName, MDAAttributeInfo.class);
        this.logger = Objects.requireNonNull(logger);
        this.storageService = DistributedServices.getDistributedStorage(Utils.getBundleContextOfObject(this), "attributes-".concat(resourceName));
        this.converter = Objects.requireNonNull(converter);
    }

    void init(final Session session,
                    final String outputQueue,
                    final boolean isTopicOutput) throws JMSException {
        this.session = session;
        //setup output queue
        if (isNullOrEmpty(outputQueue)) return;
        final Destination outputDestination = isTopicOutput ?
                session.createTopic(outputQueue) :
                session.createQueue(outputQueue);
        this.publisher = session.createProducer(outputDestination);
    }

    void setAttribute(final Message message) throws JMSException{
        final String storageKey = converter.getStorageKey(message);
        if(isNullOrEmpty(storageKey))
            throw new JMSException("storageKey is not defined");
        OpenType<?> attributeType = getAttributeType(storageKey);
        if (isNullOrEmpty(storageKey) || attributeType == null) return;
        final Object value;
        try {
            value = converter.deserialize(message, attributeType);
        } catch (final OpenDataException e) {
            throw JMSExceptionUtils.wrap("Incorrect JMX data mapping", e);
        }
        getStorage().put(storageKey, value);
        resetAccessTime();
    }

    @Override
    protected void interceptSetAttribute(final MDAAttributeInfo attribute, final Object value) throws JMSException {
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
        return storageService;
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
