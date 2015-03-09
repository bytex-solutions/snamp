package com.itworks.snamp.testing.connectors.jmx;

import com.google.common.base.Supplier;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConnectorTrackingTest extends AbstractJmxConnectorTest<TestOpenMBean> {


    private static final class TestAdapter extends AbstractResourceAdapter{
        private static final class TestAttributesModel extends ArrayList<AttributeAccessor> implements AttributesModel{
            private static final long serialVersionUID = -4679265669843153720L;

            @Override
            public void addAttribute(final String resourceName, final String attributeName, final AttributeConnector connector) {
                try {
                    add(connector.connect(resourceName + "/" + attributeName));
                } catch (final JMException e) {
                    fail(e.getMessage());
                }
            }

            @Override
            public AttributeAccessor removeAttribute(final String resourceName, final String attributeName) {
                for(final AttributeAccessor accessor: this)
                    if(Objects.equals(resourceName + "/" + attributeName, accessor.getName()))
                        return accessor;
                return null;
            }
        }
        private static final class TestNotificationsModel extends ArrayList<MBeanNotificationInfo> implements NotificationsModel{
            private static final long serialVersionUID = 943992701825986769L;

            @Override
            public void addNotification(final String resourceName, final String category, final NotificationConnector connector) {
                try {
                    add(connector.enable(resourceName + '.' + category));
                } catch (final JMException e) {
                    fail(e.getMessage());
                }
            }

            @Override
            public MBeanNotificationInfo removeNotification(final String resourceName, final String category) {
                for(final MBeanNotificationInfo notif: this)
                    if(Objects.equals(resourceName + '.' + category, notif.getNotifTypes()[0]))
                        return notif;
                return null;
            }

            @Override
            public void handleNotification(final Notification notification, final Object handback) {

            }
        }
        private final SynchronizationEvent<Void> restartEvent = new SynchronizationEvent<>(true);


        private final TestAttributesModel attributes = new TestAttributesModel();

        private final TestNotificationsModel notifications = new TestNotificationsModel();

        private final boolean enableTracking;

        private TestAdapter(final boolean tracking) {
            super("TestAdapter");
            enableTracking = tracking;
        }

        private List<AttributeAccessor> getAttributes(){
            return attributes;
        }

        private List<MBeanNotificationInfo> getNotifications(){
            return notifications;
        }

        @Override
        protected void start(final Map<String, String> parameters) throws Exception {
            populateModel(attributes);
            populateModel(notifications);
            restartEvent.fire(null);
        }

        @Override
        protected void stop() throws Exception {
            clearModel(attributes);
            clearModel(notifications);
        }

        @Override
        protected void resourceRemoved(final String resourceName) {
            if(enableTracking) {
                clearModel(resourceName, attributes);
                clearModel(resourceName, notifications);
            }
            else super.resourceRemoved(resourceName);
        }

        @Override
        protected void resourceAdded(final String resourceName) {
            if(enableTracking)try{
                enlargeModel(resourceName, notifications);
                enlargeModel(resourceName, attributes);
                restartEvent.fire(null);
            }
            catch (final JMException e){
                fail(e.getMessage());
            }
            else super.resourceAdded(resourceName);
        }

        private SynchronizationEvent.EventAwaitor<Void> getAwaitor(){
            return restartEvent.getAwaitor();
        }

        @Override
        public Logger getLogger() {
            return Logger.getLogger("TestAdapter");
        }
    }

    public ConnectorTrackingTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    private static boolean tryStart(final AbstractResourceAdapter adapter,
                                    final Map<String, String> parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method tryStartMethod = AbstractResourceAdapter.class.getDeclaredMethod("tryStart", Map.class);
        tryStartMethod.setAccessible(true);
        return (Boolean)tryStartMethod.invoke(adapter, parameters);
    }

    @Test
    public void trackingTest() throws Exception {
        final TestAdapter adapter = new TestAdapter(true);
        ManagedResourceConnectorClient.addResourceListener(getTestBundleContext(), adapter);
        try{
            tryStart(adapter, Collections.<String, String>emptyMap());
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
            //now deactivate the resource connector. This action causes restarting the adapter
            stopResourceConnector(getTestBundleContext());
            assertTrue(adapter.getAttributes().isEmpty());
            assertTrue(adapter.getNotifications().isEmpty());
            //activate resource connector. This action causes restarting the adapter and populating models
            startResourceConnector(getTestBundleContext());
            adapter.getAwaitor().await(TimeSpan.fromSeconds(2));
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
        }
        finally {
            getTestBundleContext().removeServiceListener(adapter);
            adapter.close();
        }
    }

    @Test
    public void simpleTrackingTest() throws Exception {
        final TestAdapter adapter = new TestAdapter(false);
        ManagedResourceConnectorClient.addResourceListener(getTestBundleContext(), adapter);
        try{
            tryStart(adapter, Collections.<String, String>emptyMap());
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
            //now deactivate the resource connector. This action causes restarting the adapter
            stopResourceConnector(getTestBundleContext());
            assertTrue(adapter.getAttributes().isEmpty());
            assertTrue(adapter.getNotifications().isEmpty());
            //activate resource connector. This action causes restarting the adapter and populating models
            startResourceConnector(getTestBundleContext());
            adapter.getAwaitor().await(TimeSpan.fromSeconds(2));
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
        }
        finally {
            getTestBundleContext().removeServiceListener(adapter);
            adapter.close();
        }
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        super.afterCleanupTest(context);
    }

    @Override
    protected final void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attribute = attributeFactory.get();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("1.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("2.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("3.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("4.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("5.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("6.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("7.1", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("8.0", attribute);

        attribute = attributeFactory.get();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        attributes.put("9.0", attribute);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        events.put("19.1", event);

        event = eventFactory.get();
        event.setCategory("com.itworks.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
        events.put("20.1", event);
    }
}
