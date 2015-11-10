package com.bytex.snamp.testing.connectors.mq;

import com.bytex.snamp.Consumer;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import javax.jms.*;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Before running this test you need to install RabbitMQ locally. On Debian use the following steps:
 * 1. apt-get install rabbitmq-server
 * 2. rabbitmqctl add_vhost /test
 * 3. rabbitmqctl set_permissions -p /test guest ".*" ".*" ".*"
 * 4. rabbitmq-plugins enable rabbitmq_management
 * 5. rabbitmqctl stop
 * 6. rabbitmq-server   //start RabbitMQ again
 * 5. Go to http://localhost:15672/ and create queue named as "snampQueue"
 * 7. Ready to run test
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AMQPTest extends AbstractMQConnectorTest {
    private static final String QUEUE_NAME = "snampQueue";

    public AMQPTest(){
        super("amqp://guest:guest@snamp/test?brokerlist='localhost:5672'",
                ImmutableMap.of("inputQueueName", QUEUE_NAME));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void stringAttributeTest() throws Exception {
        runTest(new Consumer<Session, Exception>() {
            @Override
            public void accept(final Session session) throws Exception {
                final Destination output = session.createQueue(QUEUE_NAME);
                final MessageProducer producer = session.createProducer(output);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                final String expectedValue = "Frank Underwood";
                final TextMessage message = session.createTextMessage(expectedValue);
                message.setStringProperty("snampStorageKey", "string");
                message.setJMSType("write");
                producer.send(message);
                Thread.sleep(1000); //message delivery is asynchronous process
                testAttribute("1.0", TypeToken.of(String.class), expectedValue, true);
            }
        });
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("expectedType", "string");
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("expectedType", "bool");
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("expectedType", "int32");
        attributes.put("3.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("expectedType", "bigint");
        attributes.put("4.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("expectedType", "array(int32)");
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("expectedType", "dictionary");
        attribute.getParameters().put("dictionaryName", "MemoryStatus");
        attribute.getParameters().put("dictionaryItemNames", "free, total");
        attribute.getParameters().put("dictionaryItemTypes", "int32, int32");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("expectedType", "float32");
        attributes.put("8.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("expectedType", "datetime");
        attributes.put("9.0", attribute);
    }
}
