package com.bytex.snamp.connectors.mq.jms;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface JMSMessageConverter {
    String getStorageKey(final Message message) throws JMSException;
    void setStorageKey(final Message message, final String storageKey) throws JMSException;
    SnampMessageType getMessageType(final Message message) throws JMSException;
    void setMessageType(final Message message, final SnampMessageType messageType) throws JMSException;
}
