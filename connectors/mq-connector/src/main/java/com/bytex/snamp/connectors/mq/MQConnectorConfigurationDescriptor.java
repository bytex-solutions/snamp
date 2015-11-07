package com.bytex.snamp.connectors.mq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MQConnectorConfigurationDescriptor {
    private static final String USERNAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String QUEUE_NAME_PARAM = "queueName";
    private static final String TOPIC_PARAM = "topic";
    private static final String MESSAGE_SELECTOR_PARAM = "messageSelector";


    public static Connection createConnection(final ConnectionFactory factory, final Map<String, String> parameters) throws JMSException {
        if(parameters.containsKey(USERNAME_PARAM) && parameters.containsKey(PASSWORD_PARAM)){
            return factory.createConnection(parameters.get(USERNAME_PARAM), parameters.get(PASSWORD_PARAM));
        }
        return factory.createConnection();
    }

    public static String getQueueName(final Map<String, String> parameters) throws MQAbsentConfigurationParameterException {
        if(parameters.containsKey(QUEUE_NAME_PARAM))
            return parameters.get(QUEUE_NAME_PARAM);
        else throw new MQAbsentConfigurationParameterException(QUEUE_NAME_PARAM);
    }

    public static boolean isTopic(final Map<String, String> parameters){
        return parameters.containsKey(TOPIC_PARAM) &&
                Boolean.valueOf(parameters.get(TOPIC_PARAM));
    }

    public static String getMessageSelector(final Map<String, String> parameters){
        return parameters.containsKey(MESSAGE_SELECTOR_PARAM) ?
                parameters.get(MESSAGE_SELECTOR_PARAM) :
                null;
    }
}
