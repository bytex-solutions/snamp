package com.itworks.snamp.testing.connectors.jmx;

import com.google.common.base.Supplier;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.ResourceConnectorException;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
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
        private final AbstractAttributesModel<AttributeAccessor> attributesModel = new AbstractAttributesModel<AttributeAccessor>() {
            @Override
            protected AttributeAccessor createAttributeView(final String resourceName, final String userDefinedAttributeName, final AttributeAccessor accessor) {
                return accessor;
            }
        };

        private final AbstractNotificationsModel<NotificationMetadata> notificationsModel = new AbstractNotificationsModel<NotificationMetadata>() {
            @Override
            protected NotificationMetadata createNotificationView(final String resourceName, final String eventName, final NotificationMetadata notifMeta) {
                return notifMeta;
            }

            @Override
            protected void handleNotification(final String sender, final Notification notif, final NotificationMetadata notificationMetadata) {

            }
        };

        private final boolean enableTracking;

        private TestAdapter(final boolean tracking) {
            super("TestAdapter");
            enableTracking = tracking;
        }

        private Map<String, AttributeAccessor> getAttributes(){
            return attributesModel;
        }

        private Map<String, NotificationMetadata> getNotifications(){
            return notificationsModel;
        }

        @Override
        protected void start() throws Exception {
            populateModel(attributesModel);
            populateModel(notificationsModel);
        }

        @Override
        protected void stop() throws Exception {
            clearModel(attributesModel);
            clearModel(notificationsModel);
        }

        @Override
        protected void resourceRemoved(final String resourceName) {
            if(enableTracking) {
                clearModel(resourceName, attributesModel);
                clearModel(resourceName, notificationsModel);
            }
            else super.resourceRemoved(resourceName);
        }

        /**
         * Invokes when a new resource connector is activated or new resource configuration is added.
         * <p/>
         * This method will be called automatically by SNAMP infrastructure.
         * In the default implementation this method throws internal exception
         * derived from {@link UnsupportedOperationException} indicating
         * that the adapter should be restarted.
         * </p
         *
         * @param resourceName The name of the resource to be added.
         * @see #enlargeModel(String, com.itworks.snamp.adapters.AbstractResourceAdapter.AbstractAttributesModel)
         */
        @Override
        protected void resourceAdded(final String resourceName) {
            if(enableTracking)try{
                enlargeModel(resourceName, notificationsModel);
                enlargeModel(resourceName, attributesModel);
            }
            catch (final ResourceConnectorException e){
                fail(e.getMessage());
            }
            else super.resourceRemoved(resourceName);
        }

        @Override
        public Logger getLogger() {
            return Logger.getLogger("TestAdapter");
        }
    }

    public ConnectorTrackingTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    private static boolean tryStart(final AbstractResourceAdapter adapter) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method tryStartMethod = AbstractResourceAdapter.class.getDeclaredMethod("tryStart");
        tryStartMethod.setAccessible(true);
        return (Boolean)tryStartMethod.invoke(adapter);
    }

    @Test
    public void trackingTest() throws Exception {
        final TestAdapter adapter = new TestAdapter(true);
        ManagedResourceConnectorClient.addResourceListener(getTestBundleContext(), adapter);
        try{
            tryStart(adapter);
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
            //now deactivate the resource connector. This action causes restarting the adapter
            stopResourceConnector(getTestBundleContext());
            assertTrue(adapter.getAttributes().isEmpty());
            assertTrue(adapter.getNotifications().isEmpty());
            //activate resource connector. This action causes restarting the adapter and populating models
            startResourceConnector(getTestBundleContext());
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
            tryStart(adapter);
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
            //now deactivate the resource connector. This action causes restarting the adapter
            stopResourceConnector(getTestBundleContext());
            assertTrue(adapter.getAttributes().isEmpty());
            assertTrue(adapter.getNotifications().isEmpty());
            //activate resource connector. This action causes restarting the adapter and populating models
            startResourceConnector(getTestBundleContext());
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
