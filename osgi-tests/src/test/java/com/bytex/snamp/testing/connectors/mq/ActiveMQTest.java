package com.bytex.snamp.testing.connectors.mq;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;
import com.bytex.snamp.jmx.CompositeDataBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import javax.jms.*;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import java.io.File;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Date;

import com.bytex.snamp.configuration.EntityMap;


/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class ActiveMQTest extends AbstractMQConnectorTest {
    private static final String QUEUE_NAME = "snampQueue";

    public ActiveMQTest(){
        super("activemq:vm://localhost:9389",
                ImmutableMap.of("inputQueueName", QUEUE_NAME,
                        "converterScript", getPathToFileInProjectRoot("sample-groovy-scripts") + File.separator + "JMSConverter.groovy"
                ));
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
    public void booleanAttributeTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final BytesMessage message = session.createBytesMessage();
            final boolean expectedValue = true;
            message.writeBoolean(expectedValue);
            message.setStringProperty("snampStorageKey", "boolean");
            message.setJMSType("write");
            producer.send(message);
            Thread.sleep(1000); //message delivery is asynchronous process
            testAttribute("2.0", TypeToken.of(Boolean.class), expectedValue, true);
        });
    }

    @Test
    public void integerAttributeTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final Integer expectedValue = 100500;
            final ObjectMessage message = session.createObjectMessage(expectedValue);
            message.setStringProperty("snampStorageKey", "int32");
            message.setJMSType("write");
            producer.send(message);
            Thread.sleep(1000); //message delivery is asynchronous process
            testAttribute("3.0", TypeToken.of(Integer.class), expectedValue, true);
        });
    }

    @Test
    public void bigIntegerAttributeTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final BigInteger expectedValue = new BigInteger("100500");
            final BytesMessage message = session.createBytesMessage();
            message.writeBytes(expectedValue.toByteArray());
            message.setStringProperty("snampStorageKey", "bigint");
            message.setJMSType("write");
            producer.send(message);
            Thread.sleep(1000); //message delivery is asynchronous process
            testAttribute("4.0", TypeToken.of(BigInteger.class), expectedValue, true);
        });
    }

    @Test
    public void intArrayAttributeTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final int[] expectedValue = {42, 50, 67};
            final BytesMessage message = session.createBytesMessage();
            message.writeBytes(ArrayUtils.toByteArray(expectedValue));
            message.setStringProperty("snampStorageKey", "array");
            message.setJMSType("write");
            producer.send(message);
            Thread.sleep(1000); //message delivery is asynchronous process
            testAttribute("5.1", TypeToken.of(int[].class), expectedValue, ArrayUtils::strictEquals, true);
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
    public void floatAttributeTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final Float expectedValue = 100.5F;
            final BytesMessage message = session.createBytesMessage();
            message.writeFloat(expectedValue);
            message.setStringProperty("snampStorageKey", "float");
            message.setJMSType("write");
            producer.send(message);
            Thread.sleep(1000); //message delivery is asynchronous process
            testAttribute("8.0", TypeToken.of(Float.class), expectedValue, true);
        });
    }

    @Test
    public void dateAttributeTest() throws Exception {
        runTest(session -> {
            final Destination output = session.createQueue(QUEUE_NAME);
            final MessageProducer producer = session.createProducer(output);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            final Date expectedValue = new Date();
            final BytesMessage message = session.createBytesMessage();
            message.writeLong(expectedValue.getTime());
            message.setStringProperty("snampStorageKey", "date");
            message.setJMSType("write");
            producer.send(message);
            Thread.sleep(1000); //message delivery is asynchronous process
            testAttribute("9.0", TypeToken.of(Date.class), expectedValue, true);
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

    @Test
    public void configurationTest(){
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "expectedType",
                "dictionaryItemNames",
                "dictionaryItemTypes",
                "dictionaryName"
        ));
        testConfigurationDescriptor(EventConfiguration.class, ImmutableSet.of(
                "expectedType",
                "dictionaryItemNames",
                "dictionaryItemTypes",
                "dictionaryName"
        ));
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
                "expirationTime",
                "userName",
                "password",
                "inputQueueName",
                "isInputTopic",
                "messageSelector",
                "outputQueueName",
                "isOutputTopic",
                "converterScript",
                "amqpVersion"
        ));
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
