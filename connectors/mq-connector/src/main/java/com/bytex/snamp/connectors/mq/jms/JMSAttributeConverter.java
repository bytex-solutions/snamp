package com.bytex.snamp.connectors.mq.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JMSAttributeConverter extends JMSMessageConverter {
    Object deserialize(final Message message, final OpenType<?> type) throws JMSException, OpenDataException;
    Message serialize(final Object value, final Session session) throws JMSException;
}
