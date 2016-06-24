package com.bytex.snamp.connectors.mq;

import com.bytex.snamp.connectors.ManagedResourceConfigurationParser;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Map;
import static com.bytex.snamp.connectors.mq.MQResourceConnectorConfigurationDescriptor.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MQConnectorConfigurationParser extends ManagedResourceConfigurationParser {
    public Connection createConnection(final ConnectionFactory factory, final Map<String, String> parameters) throws JMSException {
        if(parameters.containsKey(USERNAME_PARAM) && parameters.containsKey(PASSWORD_PARAM)){
            return factory.createConnection(parameters.get(USERNAME_PARAM), parameters.get(PASSWORD_PARAM));
        }
        return factory.createConnection();
    }

    public String getInputQueueName(final Map<String, String> parameters) throws MQAbsentConfigurationParameterException {
        if(parameters.containsKey(INP_QUEUE_NAME_PARAM))
            return parameters.get(INP_QUEUE_NAME_PARAM);
        else throw new MQAbsentConfigurationParameterException(INP_QUEUE_NAME_PARAM);
    }

    public boolean isInputTopic(final Map<String, String> parameters){
        return parameters.containsKey(INP_TOPIC_PARAM) &&
                Boolean.valueOf(parameters.get(INP_TOPIC_PARAM));
    }

    public String getMessageSelector(final Map<String, String> parameters){
        return parameters.containsKey(MESSAGE_SELECTOR_PARAM) ?
                parameters.get(MESSAGE_SELECTOR_PARAM) :
                null;
    }

    public String getOutputQueueName(final Map<String, String> parameters) {
        if(parameters.containsKey(OUT_QUEUE_NAME_PARAM))
            return parameters.get(OUT_QUEUE_NAME_PARAM);
        return null;
    }

    public boolean isOutputTopic(final Map<String, String> parameters){
        return parameters.containsKey(OUT_TOPIC_PARAM) &&
                Boolean.valueOf(parameters.get(OUT_TOPIC_PARAM));
    }

    public String getConverterScript(final Map<String, String> parameters){
        if(parameters.containsKey(CONVERTER_SCRIPT_PARAM))
            return parameters.get(CONVERTER_SCRIPT_PARAM);
        else return null;
    }

    public String getAmqpVersion(final Map<String, String> parameters){
        return parameters.containsKey(AMQP_VERSION_PARAM) ?
                parameters.get(AMQP_VERSION_PARAM) :
                null;
    }
}
