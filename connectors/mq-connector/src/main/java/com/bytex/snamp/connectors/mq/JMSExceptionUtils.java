package com.bytex.snamp.connectors.mq;

import javax.jms.JMSException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JMSExceptionUtils {
    private JMSExceptionUtils(){

    }

    public static JMSException wrap(final String message, final Exception e){
        final JMSException jmsError = new JMSException(message);
        jmsError.setLinkedException(e);
        jmsError.initCause(e);
        return jmsError;
    }

    public static JMSException wrap(final Exception e){
        return wrap(e.getMessage(), e);
    }
}
