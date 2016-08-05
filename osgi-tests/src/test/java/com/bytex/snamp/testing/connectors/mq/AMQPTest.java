package com.bytex.snamp.testing.connectors.mq;

import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.junit.Ignore;
import org.junit.Test;

import javax.jms.*;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import java.time.Duration;

import com.bytex.snamp.configuration.EntityMap;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

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
 * @version 1.2
 * @since 1.0
 */
@Ignore
public final class AMQPTest extends AbstractMQConnectorTest {
    private static final String QUEUE_NAME = "BURL:direct://amq.direct//snampQueue?durable='true'";

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
        runTest(session -> {
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
        });
    }

    @Test
    public void dictionaryAttributeTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final CompositeData expectedValue = new CompositeDataBuilder("MemoryStatus", "dummy")
                    .put("free", "free mem", 65)
                    .put("total", "total mem", 100500)
                    .build();
            final MapMessage message = session.createMapMessage();
            message.setInt("free", 65);
            message.setInt("total", 100500);
            message.setStringProperty("snampStorageKey", "dictionary");
            message.setJMSType("write");
            producer.send(message);
            Thread.sleep(1000); //message delivery is asynchronous process
            testAttribute("6.1", TypeToken.of(CompositeData.class), expectedValue, true);
        });
    }

    @Test
    public void notificationTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final Notification notif = waitForNotification("mqn", connector -> {
                final TextMessage notif1 = session.createTextMessage();
                notif1.setStringProperty("snampCategory", "mq-notification");
                notif1.setStringProperty("snampMessage", "Frank Underwood");
                notif1.setLongProperty("snampSequenceNumber", 90L);
                notif1.setJMSType("notify");
                notif1.setText("Payload");
                producer.send(notif1);
            }, Duration.ofSeconds(3));
            assertNotNull(notif);
            assertEquals("Frank Underwood", notif.getMessage());
            assertEquals(90L, notif.getSequenceNumber());
            assertEquals("Payload", notif.getUserData());
        });
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        setFeatureName(attribute, "string");
        attribute.getParameters().put("expectedType", "string");

        attribute = attributes.getOrAdd("2.0");
        setFeatureName(attribute, "boolean");
        attribute.getParameters().put("expectedType", "bool");

        attribute = attributes.getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("expectedType", "int32");

        attribute = attributes.getOrAdd("4.0");
        setFeatureName(attribute, "bigint");
        attribute.getParameters().put("expectedType", "bigint");

        attribute = attributes.getOrAdd("5.1");
        setFeatureName(attribute, "array");
        attribute.getParameters().put("expectedType", "array(int32)");

        attribute = attributes.getOrAdd("6.1");
        setFeatureName(attribute, "dictionary");
        attribute.getParameters().put("expectedType", "dictionary");
        attribute.getParameters().put("dictionaryName", "MemoryStatus");
        attribute.getParameters().put("dictionaryItemNames", "free, total");
        attribute.getParameters().put("dictionaryItemTypes", "int32, int32");

        attribute = attributes.getOrAdd("8.0");
        setFeatureName(attribute, "float");
        attribute.getParameters().put("expectedType", "float32");

        attribute = attributes.getOrAdd("9.0");
        setFeatureName(attribute, "date");
        attribute.getParameters().put("expectedType", "datetime");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("mqn");
        setFeatureName(event, "mq-notification");
        event.getParameters().put("severity", "notice");
        event.getParameters().put("expectedType", "string");
    }
}
