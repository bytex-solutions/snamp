package com.bytex.snamp.connector.mq.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface JMSNotificationConverter extends JMSMessageConverter {
    /**
     * Parses message payload.
     * @param message JMS message to convert.
     * @return Human-readable message associated with notification.
     * @throws JMSException Internal JMS error.
     */
    String getMessage(final Message message) throws JMSException;

    String getCategory(final Message message) throws JMSException;

    long getSequenceNumber(final Message message) throws JMSException;

    /**
     * Parses attachment of the notification.
     * @param message JMS message to convert.
     * @param type Expected type of attachment.
     * @return Attachment object.
     * @throws JMSException Internal JMS error.
     * @throws OpenDataException Conversion error.
     */
    Object deserialize(final Message message, final OpenType<?> type) throws JMSException, OpenDataException;
}
