package com.itworks.snamp.testing.connectors;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagedResourceConnectorBean;
import com.itworks.snamp.connectors.WellKnownTypeSystem;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.connectors.notifications.NotificationUtils.SynchronizationListener;
import static com.itworks.snamp.connectors.notifications.NotificationUtils.generateListenerId;

/**
 * Represents tests for {@link com.itworks.snamp.connectors.ManagedResourceConnectorBean} class.
 * @author Roman Sakno
 */
public final class ManagedResourceConnectorBeanTest extends AbstractUnitTest<ManagedResourceConnectorBean> {

    private static final class TestManagementConnectorBeanTest extends ManagedResourceConnectorBean {
        private String field1;
        private int field2;
        private boolean field3;

        public TestManagementConnectorBeanTest() throws IntrospectionException {
            super(new WellKnownTypeSystem(), null);
        }

        public final String getProperty1(){
            return field1;
        }

        public final void setProperty1(final String value){
            field1 = value;
            emitPropertyChanged("property1");
        }

        public final int getProperty2(){
            return field2;
        }

        public final void setProperty2(final int value){
            field2 = value;
            emitPropertyChanged("property2");
        }

        public final boolean getProperty3(){
            return field3;
        }

        public final void setProperty3(final boolean value){
            field3 = value;
            emitPropertyChanged("property3");
        }

        protected final void emitPropertyChanged(final String propertyName){
            emitNotification("propertyChanged", Notification.Severity.NOTICE, String.format("Property %s is changed", propertyName), null);
        }
    }

    public ManagedResourceConnectorBeanTest(){
        super(ManagedResourceConnectorBean.class);
    }

    @Test
    public final void testConnectorBean() throws IntrospectionException, TimeoutException, InterruptedException {
        final TestManagementConnectorBeanTest connector = new TestManagementConnectorBeanTest();
        connector.setProperty1("123");
        assertNotNull(connector.connectAttribute("0", "property1", new HashMap<String, String>()));
        //enables notifications
        assertNotNull(connector.enableNotifications("list1", "propertyChanged", null));
        final SynchronizationListener listener = new SynchronizationListener();
        connector.subscribe(generateListenerId(listener), listener, false);
        assertEquals(connector.getProperty1(), connector.getAttribute("0", TimeSpan.INFINITE, ""));
        connector.setAttribute("0", TimeSpan.INFINITE, "1234567890");
        final Notification n = listener.getAwaitor().await(new TimeSpan(10, TimeUnit.SECONDS));
        assertNotNull(n);
        assertEquals(Notification.Severity.NOTICE, n.getSeverity());
        assertEquals("Property property1 is changed", n.getMessage());
        assertEquals(connector.getProperty1(), connector.getAttribute("0", TimeSpan.INFINITE, ""));
        final AttributeMetadata md = connector.getAttributeInfo("0");
        assertTrue(md.canRead());
        assertTrue(md.canWrite());
        assertEquals("property1", md.getName());
        assertNotNull(md.getType().getProjection(String.class));
    }

    @Test
    public final void testAnonymousBean() throws IntrospectionException, TimeoutException{
        final ManagedResourceConnectorBean mc = ManagedResourceConnectorBean.wrap(new Object() {
            private int simpleField;

            public final int getProperty() {
                return simpleField;
            }

            public final void setProperty(int value) {
                simpleField = value;
            }
        }, new WellKnownTypeSystem());
        assertNotNull(mc.connectAttribute("1", "property", new HashMap<String, String>()));
        assertTrue(mc.setAttribute("1", TimeSpan.INFINITE, 42));
        assertEquals(42, mc.getAttribute("1", TimeSpan.INFINITE, 0));
    }
}