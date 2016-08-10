package com.bytex.snamp.connector.mq.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class JMSMessageListener implements MessageListener {
    private final JMSAttributeRepository attributes;
    private final JMSNotificationRepository notifications;
    private final JMSMessageConverter converter;
    private final Logger logger;

    JMSMessageListener(final JMSAttributeRepository attrs,
                       final JMSNotificationRepository notifs,
                       final JMSMessageConverter dataConverter,
                       final Logger logger){
        this.attributes = Objects.requireNonNull(attrs);
        this.notifications = Objects.requireNonNull(notifs);
        this.converter = Objects.requireNonNull(dataConverter);
        this.logger = logger;
    }

    @Override
    public void onMessage(final Message message) {
        try {
            switch (converter.getMessageType(message)){
                case WRITE:
                    attributes.setAttribute(message);
                    break;
                case NOTIFICATION:
                    notifications.fire(message);
            }
        } catch (final JMSException e) {
            logger.log(Level.SEVERE, "Unable to process message " + message, e);
        }
    }
}
