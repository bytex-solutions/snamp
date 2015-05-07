package com.itworks.snamp.connectors;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AttributeValue;
import com.itworks.snamp.concurrent.Awaitor;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.CustomAttributeInfo;
import com.itworks.snamp.connectors.discovery.DiscoveryService;
import com.itworks.snamp.connectors.notifications.SynchronizationListener;
import com.itworks.snamp.internal.annotations.SpecialUse;
import org.junit.Assert;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.Notification;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

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
            public String getDescription(final Locale locale) {
                return "None";
            }
        }

        static final class Property1Formatter implements ManagementAttributeFormatter<String>{

            @Override
            public SimpleType<String> getAttributeType() {
                return SimpleType.STRING;
            }

            @Override
            public String toJmxValue(final Object attributeValue, final CustomAttributeInfo metadata) {
                assertEquals("property1", metadata.getDescriptor().getAttributeName());
                return attributeValue.toString();
            }

            @Override
            public Object fromJmxValue(final String jmxValue, final CustomAttributeInfo metadata) {
                assertEquals("property1", metadata.getDescriptor().getAttributeName());
                return jmxValue;
            }
        }

        private String field1;
        private int field2;
        private boolean field3;

        public TestManagementConnectorBean() throws IntrospectionException {
            super("TestResource", EnumSet.allOf(TestNotificationType.class));
        }

        /**
         * Gets a logger associated with this platform service.
         *
         * @return A logger associated with this platform service.
         */
        @Override
        public Logger getLogger() {
            return Logger.getAnonymousLogger();
        }

        @SpecialUse
        @ManagementAttribute(formatter = Property1Formatter.class)
        public final String getProperty1() {
            return field1;
        }

        @SpecialUse
        public final void setProperty1(final String value) {
            field1 = value;
            emitPropertyChanged("property1");
        }

        @SpecialUse
        public final int getProperty2() {
            return field2;
        }

        @SpecialUse
        public final void setProperty2(final int value) {
            field2 = value;
            emitPropertyChanged("property2");
        }

        @SpecialUse
        public final boolean getProperty3() {
            return field3;
        }

        @SpecialUse
        public final void setProperty3(final boolean value) {
            field3 = value;
            emitPropertyChanged("property3");
        }

        protected final void emitPropertyChanged(final String propertyName) {
            emitNotification(TestNotificationType.PROPERTY_CHANGED, String.format("Property %s is changed", propertyName), "Attachment string");
        }
    }

    @Test
    public void discoveryTest() throws IntrospectionException {
        final TestManagementConnectorBean connector = new TestManagementConnectorBean();
        final DiscoveryService discovery = connector.createDiscoveryService();
        final Collection<AttributeConfiguration> attributes =
                discovery.discover("", ImmutableMap.<String, String>of(), AttributeConfiguration.class);
        assertEquals(3, attributes.size());
        final Collection<EventConfiguration> events =
                discovery.discover("", ImmutableMap.<String, String>of(), EventConfiguration.class);
        assertEquals(1, events.size());
    }

    @Test
    public final void testConnectorBean() throws IntrospectionException, JMException, InterruptedException, TimeoutException {
        final TestManagementConnectorBean connector = new TestManagementConnectorBean();
        connector.field1 = "123";
        final MBeanAttributeInfo md;
        assertNotNull(md = connector.addAttribute("0", "property1", TimeSpan.fromSeconds(1), ConfigParameters.empty()));
        //enables notifications
        assertNotNull(connector.enableNotifications("list1", "propertyChanged", ConfigParameters.empty()));
        final SynchronizationListener listener = new SynchronizationListener();
        final Awaitor<Notification, ExceptionPlaceholder> notifAwaitor = listener.getAwaitor();
        connector.addNotificationListener(listener, null, null);
        assertEquals(connector.field1, connector.getAttribute("0"));
        connector.setAttribute(new AttributeValue("0", "1234567890", SimpleType.STRING));
        final Notification n = notifAwaitor.await(new TimeSpan(10, TimeUnit.SECONDS));
        assertNotNull(n);
        assertEquals("Property property1 is changed", n.getMessage());
        assertEquals("Attachment string", n.getUserData());
        assertEquals(connector.field1, connector.getAttribute("0"));
        assertTrue(md.isReadable());
        assertTrue(md.isWritable());
        assertEquals("property1", AttributeDescriptor.getAttributeName(md));
        assertEquals(SimpleType.STRING, AttributeDescriptor.getOpenType(md));
    }

    @Test
    public void connectionParamsHashCodeTest(){
        final String connectionString = "aaa";
        final ImmutableMap<String, String> params1 = ImmutableMap.of("1", "value", "2", "value2");
        final ImmutableMap<String, String> params2 = ImmutableMap.of("2", "value2", "1", "value");
        assertEquals(params1, params2);
        assertEquals(ManagedResourceActivator.computeConnectionParamsHashCode(connectionString, params1),
                ManagedResourceActivator.computeConnectionParamsHashCode(connectionString, params2));
    }
}
