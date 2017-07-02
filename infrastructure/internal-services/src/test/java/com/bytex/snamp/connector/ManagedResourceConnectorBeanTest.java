package com.bytex.snamp.connector;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.attributes.reflection.ManagementAttribute;
import com.bytex.snamp.connector.attributes.reflection.ManagementAttributeMarshaller;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.reflection.ManagementOperation;
import com.bytex.snamp.connector.operations.reflection.OperationParameter;
import com.bytex.snamp.gateway.modeling.AttributeValue;
import org.junit.Assert;
import org.junit.Test;

import javax.management.*;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.beans.IntrospectionException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.newEntityConfiguration;

/**
 * Represents tests for {@link ManagedResourceConnectorBean} class.
 * @author Roman Sakno
 */
public final class ManagedResourceConnectorBeanTest extends Assert {
    public static final class TestManagementConnectorBean extends ManagedResourceConnectorBean {
        private enum TestNotificationType implements ManagementNotificationType<String>{
            PROPERTY_CHANGED;

            @Override
            public OpenType<String> getUserDataType() {
                return SimpleType.STRING;
            }

            @Override
            public String getCategory() {
                return "propertyChanged";
            }

            @Override
            public String toString(final Locale locale) {
                return "None";
            }
        }

        public static final class Property1Marshaller implements ManagementAttributeMarshaller<String> {

            @Override
            public SimpleType<String> getOpenType() {
                return SimpleType.STRING;
            }

            @Override
            public String toJmxValue(final Object attributeValue, final MBeanAttributeInfo metadata) {
                assertEquals("property1", AttributeDescriptor.getName(metadata));
                return attributeValue.toString();
            }

            @Override
            public Object fromJmxValue(final String jmxValue, final MBeanAttributeInfo metadata) {
                assertEquals("property1", AttributeDescriptor.getName(metadata));
                return jmxValue;
            }
        }

        private String field1;
        private int field2;
        private boolean field3;

        TestManagementConnectorBean() throws IntrospectionException {
            super("TestResource", EnumSet.allOf(TestNotificationType.class));
        }

        @SpecialUse(SpecialUse.Case.REFLECTION)
        @ManagementAttribute(marshaller = Property1Marshaller.class)
        public String getProperty1() {
            return field1;
        }

        @SpecialUse(SpecialUse.Case.REFLECTION)
        public void setProperty1(final String value) {
            field1 = value;
            emitPropertyChanged("property1");
        }

        @SpecialUse(SpecialUse.Case.REFLECTION)
        @ManagementAttribute
        public int getProperty2() {
            return field2;
        }

        @SpecialUse(SpecialUse.Case.REFLECTION)
        public void setProperty2(final int value) {
            field2 = value;
            emitPropertyChanged("property2");
        }

        @SpecialUse(SpecialUse.Case.REFLECTION)
        @ManagementAttribute
        public boolean getProperty3() {
            return field3;
        }

        @SpecialUse(SpecialUse.Case.REFLECTION)
        public void setProperty3(final boolean value) {
            field3 = value;
            emitPropertyChanged("property3");
        }

        private void emitPropertyChanged(final String propertyName) {
            emitNotification(TestNotificationType.PROPERTY_CHANGED, String.format("Property %s is changed", propertyName), "Attachment string");
        }

        @ManagementOperation(description = "Computes sum of two integers", impact = MBeanOperationInfo.INFO)
        public int computeSum(@OperationParameter(name = "x", description = "First operand") final int x,
                              final int y){
            return x + y;
        }
    }

    @Test
    public void discoveryTest() throws IntrospectionException {
        final TestManagementConnectorBean connector = new TestManagementConnectorBean();
        final Map<String, AttributeDescriptor> attributes = connector.queryObject(AttributeSupport.class)
                .map(AttributeSupport::discoverAttributes)
                .orElseGet(Collections::emptyMap);
        assertEquals(3, attributes.size());
        final Map<String, NotificationDescriptor> events = connector.queryObject(NotificationSupport.class)
                .map(NotificationSupport::discoverNotifications)
                .orElseGet(Collections::emptyMap);
        assertEquals(1, events.size());
    }

    private static AttributeDescriptor makeAttributeConfig(final String name){
        final AttributeConfiguration result = newEntityConfiguration(AttributeConfiguration.class);
        assertNotNull(result);
        result.setAlternativeName(name);
        return new AttributeDescriptor(result);
    }

    private static NotificationDescriptor makeEventConfig(final String name){
        final EventConfiguration result = newEntityConfiguration(EventConfiguration.class);
        assertNotNull(result);
        result.setAlternativeName(name);
        return new NotificationDescriptor(result);
    }

    private static OperationDescriptor makeOperationConfig(final String name){
        final OperationConfiguration result = newEntityConfiguration(OperationConfiguration.class);
        assertNotNull(result);
        result.setAlternativeName(name);
        return new OperationDescriptor(result);
    }

    @Test
    public void testConnectorBean() throws Exception {
        final TestManagementConnectorBean connector = new TestManagementConnectorBean();
        connector.field1 = "123";
        final MBeanAttributeInfo md;
        md = connector.getAttributeSupport().addAttribute("p1", makeAttributeConfig("property1")).orElseThrow(AssertionError::new);
        //enables notifications
        assertNotNull(connector.getNotificationSupport().enableNotifications("propertyChanged", makeEventConfig("propertyChanged")));
        final Mailbox listener = MailboxFactory.newMailbox();
        connector.queryObject(NotificationSupport.class).orElseThrow(AssertionError::new).addNotificationListener(listener, listener, null);
        assertEquals(connector.getProperty1(), connector.getAttribute("p1"));
        connector.setAttribute(new AttributeValue("p1", "1234567890", SimpleType.STRING));
        final Notification n = listener.poll(10, TimeUnit.SECONDS);
        assertNotNull(n);
        assertEquals("Property property1 is changed", n.getMessage());
        assertEquals("Attachment string", n.getUserData());
        assertEquals(connector.getProperty1(), connector.getAttribute("p1"));
        assertTrue(md.isReadable());
        assertTrue(md.isWritable());
        assertEquals("p1", md.getName());
        assertEquals(SimpleType.STRING, AttributeDescriptor.getOpenType(md));
        //enables operations
        assertNotNull(connector.getOperationSupport().enableOperation("cs", makeOperationConfig("computeSum")));
        final Object result = connector.invoke("cs", new Object[]{4, 5}, ArrayUtils.emptyArray(String[].class));
        assertTrue(result instanceof Integer);
        assertEquals(9, result);
    }

    @Test
    public void smartModeTest() throws IntrospectionException, JMException {
        final TestManagementConnectorBean connector = new TestManagementConnectorBean();
        assertFalse(connector.expandAll().isEmpty());
        connector.setAttribute(new Attribute("property1", "Frank Underwood"));
        assertEquals("Frank Underwood", connector.getProperty1());
        assertEquals("Frank Underwood", connector.getAttribute("property1"));
        assertEquals(connector.getProperty1(), connector.getAttribute("property1"));
    }
}
