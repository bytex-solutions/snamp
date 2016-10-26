package com.bytex.snamp.connector.mq.jms;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface JMSMessageConverter {
    String getStorageKey(final Message message) throws JMSException;
    void setStorageKey(final Message message, final String storageKey) throws JMSException;
    SnampMessageType getMessageType(final Message message) throws JMSException;
    void setMessageType(final Message message, final SnampMessageType messageType) throws JMSException;
}
