package com.bytex.snamp.testing.connectors.jmx;

import com.bytex.snamp.adapters.AbstractResourceAdapter;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.adapters.modeling.FeatureAccessor;
import com.bytex.snamp.adapters.modeling.NotificationAccessor;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ConnectorTrackingTest extends AbstractJmxConnectorTest<TestOpenMBean> {


    private static final class TestAdapter extends AbstractResourceAdapter{

        private final ArrayList<AttributeAccessor> attributes = new ArrayList<>();

        private final ArrayList<NotificationAccessor> notifications = new ArrayList<>();

        private TestAdapter() {
            super("TestAdapter");
        }

        private List<AttributeAccessor> getAttributes(){
            return attributes;
        }

        private List<NotificationAccessor> getNotifications(){
            return notifications;
        }

        @Override
        protected void start(final Map<String, String> parameters) {
        }

        @Override
        protected void stop() {
            attributes.clear();
            notifications.clear();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
            if(feature instanceof MBeanAttributeInfo){
                final AttributeAccessor accessor = new AttributeAccessor((MBeanAttributeInfo)feature);
                attributes.add(accessor);
                return (FeatureAccessor<M>)accessor;
            }
            else if(feature instanceof MBeanNotificationInfo){
                final NotificationAccessor accessor = new NotificationAccessor((MBeanNotificationInfo)feature) {
                    @Override
                    public void handleNotification(final Notification notification, final Object handback) {

                    }
                };
                notifications.add(accessor);
                return (FeatureAccessor<M>)accessor;
            }
            else return null;
        }

        @Override
        protected Iterable<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
            try {
                return Iterables.concat(ImmutableList.copyOf(attributes),
                        ImmutableList.copyOf(notifications));
            }
            finally {
                attributes.clear();
                notifications.clear();
            }
        }

        private AttributeAccessor removeAttribute(final MBeanAttributeInfo metadata){
             return AttributeAccessor.remove(attributes, metadata);
        }

        private NotificationAccessor removeNotification(final MBeanNotificationInfo metadata){
            return NotificationAccessor.remove(notifications, metadata);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
            if(feature instanceof MBeanAttributeInfo)
                return (FeatureAccessor<M>)removeAttribute((MBeanAttributeInfo)feature);
            else if(feature instanceof MBeanNotificationInfo)
                return (FeatureAccessor<M>)removeNotification((MBeanNotificationInfo)feature);
            else return null;
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

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void simpleTrackingTest() throws Exception {
        final TestAdapter adapter = new TestAdapter();
        ManagedResourceConnectorClient.addResourceListener(getTestBundleContext(), adapter);
        try {
            tryStart(adapter, Collections.emptyMap());
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
            //now deactivate the resource connector. This action causes restarting the adapter
            stopResourceConnector(getTestBundleContext());
            assertTrue(adapter.getAttributes().isEmpty());
            assertTrue(adapter.getNotifications().isEmpty());
            //activate resource connector. This action causes registration of features
            startResourceConnector(getTestBundleContext());
            //...but this process is asynchronous
            Thread.sleep(2000);
            assertEquals(9, adapter.getAttributes().size());
            assertEquals(2, adapter.getNotifications().size());
            //remove some attributes
            processConfiguration(config -> {
                final ManagedResourceConfiguration testResource =
                        config.getEntities(ManagedResourceConfiguration.class).get(TEST_RESOURCE_NAME);
                assertNotNull(testResource);
                assertNotNull(testResource.getFeatures(AttributeConfiguration.class).remove("1.0"));
                assertNotNull(testResource.getFeatures(AttributeConfiguration.class).remove("2.0"));
                assertNotNull(testResource.getFeatures(AttributeConfiguration.class).remove("3.0"));
                assertNotNull(testResource.getFeatures(AttributeConfiguration.class).remove("4.0"));
                assertNotNull(testResource.getFeatures(EventConfiguration.class).remove("19.1"));
                return true;
            });
            Thread.sleep(2000);
            assertEquals(5, adapter.getAttributes().size());
            assertEquals(1, adapter.getNotifications().size());
        } finally {
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
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("1.0");
        setFeatureName(attribute, "string");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        setFeatureName(attribute, "boolean");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        setFeatureName(attribute, "int32");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("4.0");
        setFeatureName(attribute, "bigint");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        setFeatureName(attribute, "array");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("6.1");
        setFeatureName(attribute, "dictionary");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("7.1");
        setFeatureName(attribute, "table");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        setFeatureName(attribute, "float");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("9.0");
        setFeatureName(attribute, "date");
        attribute.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        setFeatureName(event, AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);

        event = events.getOrAdd("20.1");
        setFeatureName(event, "com.bytex.snamp.connectors.tests.impl.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", TestOpenMBean.BEAN_NAME);
    }
}
