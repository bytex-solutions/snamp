package com.snamp.connectors;

import com.snamp.SnampClassTestSet;
import com.snamp.TimeSpan;
import com.snamp.connectors.util.NotificationUtils;
import static com.snamp.connectors.NotificationSupport.Notification;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents tests for {@link ManagementConnectorBean} class.
 * @author Roman Sakno
 */
public final class ManagementConnectorBeanTest extends SnampClassTestSet<ManagementConnectorBean> {

    private static final class TestManagementConnectorBeanTest extends ManagementConnectorBean {
        private String field1;
        private int field2;
        private boolean field3;

        public TestManagementConnectorBeanTest() throws IntrospectionException {
            super(new WellKnownTypeSystem());
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

    @Test
    public final void testConnectorBean() throws IntrospectionException, TimeoutException, InterruptedException {
        final TestManagementConnectorBeanTest connector = new TestManagementConnectorBeanTest();
        connector.setProperty1("123");
        connector.connectAttribute("0", "property1", new HashMap<String, String>());
        //enables notifications
        connector.enableNotifications("list1", "propertyChanged", null);
        final NotificationUtils.SynchronizationListener listener = new NotificationUtils.SynchronizationListener();
        connector.subscribe("list1", listener);
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
        final ManagementConnector mc = ManagementConnectorBean.wrap(new Object() {
            private int simpleField;

            public final int getProperty() {
                return simpleField;
            }

            public final void setProperty(int value) {
                simpleField = value;
            }
        }, new WellKnownTypeSystem());
        mc.connectAttribute("1", "property", new HashMap<String, String>());
        mc.setAttribute("1", TimeSpan.INFINITE, 42);
        assertEquals(42, mc.getAttribute("1", TimeSpan.INFINITE, 0));
    }
}
