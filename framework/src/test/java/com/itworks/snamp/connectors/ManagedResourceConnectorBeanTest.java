package com.itworks.snamp.connectors;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AttributeValue;
import com.itworks.snamp.concurrent.Awaitor;
import com.itworks.snamp.configuration.ConfigParameters;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.notifications.SynchronizationListener;
import org.junit.Assert;
import org.junit.Test;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.Notification;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.beans.IntrospectionException;
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
    private static final class TestManagementConnectorBean extends ManagedResourceConnectorBean {
        private static enum TestNotificationType implements ManagementNotificationType<String>{
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

        private String field1;
        private int field2;
        private boolean field3;

        public TestManagementConnectorBean() throws IntrospectionException {
            super(EnumSet.allOf(TestNotificationType.class));
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

        public final String getProperty1() {
            assertEquals("property1", AttributeContext.get().getDeclaredAttributeName());
            assertNotNull(AttributeContext.get().getOperationTimeout());
            return field1;
        }

        public final void setProperty1(final String value) {
            assertEquals("property1", AttributeContext.get().getDeclaredAttributeName());
            assertNotNull(AttributeContext.get().getOperationTimeout());
            field1 = value;
            emitPropertyChanged("property1");
        }

        public final int getProperty2() {
            assertEquals("property2", AttributeContext.get().getDeclaredAttributeName());
            assertNotNull(AttributeContext.get().getOperationTimeout());
            return field2;
        }

        public final void setProperty2(final int value) {
            assertEquals("property2", AttributeContext.get().getDeclaredAttributeName());
            field2 = value;
            emitPropertyChanged("property2");
        }

        public final boolean getProperty3() {
            assertEquals("property3", AttributeContext.get().getDeclaredAttributeName());
            return field3;
        }

        public final void setProperty3(final boolean value) {
            assertEquals("property3", AttributeContext.get().getDeclaredAttributeName());
            field3 = value;
            emitPropertyChanged("property3");
        }

        protected final void emitPropertyChanged(final String propertyName) {
            emitNotification(TestNotificationType.PROPERTY_CHANGED, String.format("Property %s is changed", propertyName), "Attachment string");
        }
    }

    @Test
    public final void testConnectorBean() throws IntrospectionException, JMException, InterruptedException, TimeoutException {
        final TestManagementConnectorBean connector = new TestManagementConnectorBean();
        connector.field1 = "123";
        final MBeanAttributeInfo md;
        assertNotNull(md = connector.connectAttribute("0", "property1", TimeSpan.fromSeconds(1), ConfigParameters.empty()));
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
        assertEquals(TestManagementConnectorBean.computeConnectionParamsHashCode(connectionString, params1),
                TestManagementConnectorBean.computeConnectionParamsHashCode(connectionString, params2));
    }
}
