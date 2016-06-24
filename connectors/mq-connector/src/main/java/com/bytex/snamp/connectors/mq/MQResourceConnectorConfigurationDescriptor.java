package com.bytex.snamp.connectors.mq;

import com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.THREAD_POOL_KEY;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class MQResourceConnectorConfigurationDescriptor extends MDAResourceConfigurationDescriptorProvider {
    static final String USERNAME_PARAM = "userName";
    static final String PASSWORD_PARAM = "password";
    static final String INP_QUEUE_NAME_PARAM = "inputQueueName";
    static final String INP_TOPIC_PARAM = "isInputTopic";
    static final String MESSAGE_SELECTOR_PARAM = "messageSelector";
    static final String OUT_QUEUE_NAME_PARAM = "outputQueueName";
    static final String OUT_TOPIC_PARAM = "isOutputTopic";
    static final String CONVERTER_SCRIPT_PARAM = "converterScript";
    static final String AMQP_VERSION_PARAM = "amqpVersion";

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

    MQResourceConnectorConfigurationDescriptor() {
        super(new MQConnectorConfigurationDescriptor(),
                new MQAttributeConfigurationDescriptor(),
                new MQEventConfigurationDescriptor());
    }


}
