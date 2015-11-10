package com.bytex.snamp.connectors.mq;

import com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MQConnectorConfigurationDescriptor extends MDAResourceConfigurationDescriptorProvider {
    private static final String USERNAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String INP_QUEUE_NAME_PARAM = "inputQueueName";
    private static final String INP_TOPIC_PARAM = "isInputTopic";
    private static final String MESSAGE_SELECTOR_PARAM = "messageSelector";
    private static final String OUT_QUEUE_NAME_PARAM = "outputQueueName";
    private static final String OUT_TOPIC_PARAM = "isOutputTopic";
    private static final String CONVERTER_SCRIPT_PARAM = "converterScript";
    private static final String AMQP_VERSION_PARAM = "amqpVersion";

    public static Connection createConnection(final ConnectionFactory factory, final Map<String, String> parameters) throws JMSException {
        if(parameters.containsKey(USERNAME_PARAM) && parameters.containsKey(PASSWORD_PARAM)){
            return factory.createConnection(parameters.get(USERNAME_PARAM), parameters.get(PASSWORD_PARAM));
        }
        return factory.createConnection();
    }

    public static String getInputQueueName(final Map<String, String> parameters) throws MQAbsentConfigurationParameterException {
        if(parameters.containsKey(INP_QUEUE_NAME_PARAM))
            return parameters.get(INP_QUEUE_NAME_PARAM);
        else throw new MQAbsentConfigurationParameterException(INP_QUEUE_NAME_PARAM);
    }

    public static boolean isInputTopic(final Map<String, String> parameters){
        return parameters.containsKey(INP_TOPIC_PARAM) &&
                Boolean.valueOf(parameters.get(INP_TOPIC_PARAM));
    }

    public static String getMessageSelector(final Map<String, String> parameters){
        return parameters.containsKey(MESSAGE_SELECTOR_PARAM) ?
                parameters.get(MESSAGE_SELECTOR_PARAM) :
                null;
    }

    public static String getOutputQueueName(final Map<String, String> parameters) {
        if(parameters.containsKey(OUT_QUEUE_NAME_PARAM))
            return parameters.get(OUT_QUEUE_NAME_PARAM);
        return null;
    }

    public static boolean isOutputTopic(final Map<String, String> parameters){
        return parameters.containsKey(OUT_TOPIC_PARAM) &&
                Boolean.valueOf(parameters.get(OUT_TOPIC_PARAM));
    }

    public static String getConverterScript(final Map<String, String> parameters){
        if(parameters.containsKey(CONVERTER_SCRIPT_PARAM))
            return parameters.get(CONVERTER_SCRIPT_PARAM);
        else return null;
    }

    public static String getAmqpVersion(final Map<String, String> parameters){
        return parameters.containsKey(AMQP_VERSION_PARAM) ?
                parameters.get(AMQP_VERSION_PARAM) :
                null;
    }
}
