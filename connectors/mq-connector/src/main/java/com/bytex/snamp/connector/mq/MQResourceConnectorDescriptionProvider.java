package com.bytex.snamp.connector.mq;

import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.mda.MDAResourceConfigurationDescriptorProvider;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.util.Map;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class MQResourceConnectorDescriptionProvider extends MDAResourceConfigurationDescriptorProvider implements ManagedResourceDescriptionProvider {
    private static final String USERNAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String INP_QUEUE_NAME_PARAM = "inputQueueName";
    private static final String INP_TOPIC_PARAM = "isInputTopic";
    private static final String MESSAGE_SELECTOR_PARAM = "messageSelector";
    private static final String OUT_QUEUE_NAME_PARAM = "outputQueueName";
    private static final String OUT_TOPIC_PARAM = "isOutputTopic";
    private static final String CONVERTER_SCRIPT_PARAM = "converterScript";
    private static final String AMQP_VERSION_PARAM = "amqpVersion";

    private static final class MQConnectorConfigurationDescriptor extends ConnectorConfigurationDescriptor{
        private static final String RESOURCE_NAME = "MQConnectorConfig";

        private MQConnectorConfigurationDescriptor(){
            super(RESOURCE_NAME,
                    USERNAME_PARAM,
                    PASSWORD_PARAM,
                    INP_QUEUE_NAME_PARAM,
                    INP_TOPIC_PARAM,
                    MESSAGE_SELECTOR_PARAM,
                    OUT_QUEUE_NAME_PARAM,
                    OUT_TOPIC_PARAM,
                    CONVERTER_SCRIPT_PARAM,
                    AMQP_VERSION_PARAM,
                    THREAD_POOL_KEY);
        }
    }

    private static final class MQAttributeConfigurationDescriptor extends AttributeConfigurationDescriptor{
        private static final String RESOURCE_NAME = "MQAttributeConfig";

        private MQAttributeConfigurationDescriptor(){
            super(RESOURCE_NAME);
        }
    }

    private static final class MQEventConfigurationDescriptor extends EventConfigurationDescriptor{
        private static final String RESOURCE_NAME = "MQEventConfig";

        private MQEventConfigurationDescriptor(){
            super(RESOURCE_NAME);
        }
    }

    private static final LazyValue<MQResourceConnectorDescriptionProvider> INSTANCE = LazyValueFactory.THREAD_SAFE.of(MQResourceConnectorDescriptionProvider::new);

    private MQResourceConnectorDescriptionProvider() {
        super(new MQConnectorConfigurationDescriptor(),
                new MQAttributeConfigurationDescriptor(),
                new MQEventConfigurationDescriptor());
    }

    public static MQResourceConnectorDescriptionProvider getInstance(){
        return INSTANCE.get();
    }

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
