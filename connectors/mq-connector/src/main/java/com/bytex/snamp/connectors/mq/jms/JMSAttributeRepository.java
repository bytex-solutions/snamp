package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.mda.MDAAttributeRepository;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JMSAttributeRepository extends MDAAttributeRepository<JMSAttributeAccessor> implements MessageListener {
    private static final String ATTRIBUTE_NAME = "snampAttributeName";
    static Class<JMSAttributeAccessor> FEATURE_TYPE = JMSAttributeAccessor.class;

    JMSAttributeRepository(final String resourceName) {
        super(resourceName, FEATURE_TYPE);
    }

    @Override
    public void onMessage(final Message message) {
        try {
            final String attributeID = message.getStringProperty(ATTRIBUTE_NAME);
        } catch (final JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connects attribute with this repository.
     *
     * @param attributeID User-defined identifier of the attribute.
     * @param descriptor  Metadata of the attribute.
     * @return Constructed attribute object.
     * @throws Exception Internal connector error.
     */
    @Override
    protected JMSAttributeAccessor createAttributeMetadata(String attributeID, AttributeDescriptor descriptor) throws Exception {
        return null;
    }

    /**
     * Gets storage used to read/write attribute values received from external Agents.
     *
     * @return The storage used to read/write attributes.
     */
    @Override
    protected ConcurrentMap<String, Object> getStorage() {
        return null;
    }


    @Override
    protected Logger getLogger() {
        return null;
    }
}
