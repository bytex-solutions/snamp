package com.itworks.snamp.testing.connectors.jmx;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.adapters.modeling.AttributeAccessor;
import com.itworks.snamp.adapters.modeling.FeatureAccessor;
import com.itworks.snamp.adapters.modeling.NotificationAccessor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
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
        protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws Exception {
            if(feature instanceof MBeanAttributeInfo){
                final AttributeAccessor accessor = new AttributeAccessor((MBeanAttributeInfo)feature);
                attributes.add(accessor);
                return (FeatureAccessor<M, S>)accessor;
            }
            else if(feature instanceof MBeanNotificationInfo){
                final NotificationAccessor accessor = new NotificationAccessor((MBeanNotificationInfo)feature) {
                    @Override
                    public void handleNotification(final Notification notification, final Object handback) {

                    }
                };
                notifications.add(accessor);
                return (FeatureAccessor<M, S>)accessor;
            }
            else return null;
        }

        @Override
        protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) throws Exception {
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

        @Override
        protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) throws Exception {
            if(feature instanceof MBeanAttributeInfo)
                return (FeatureAccessor<M, ?>)removeAttribute((MBeanAttributeInfo)feature);
            else if(feature instanceof MBeanNotificationInfo)
                return (FeatureAccessor<M, ?>)removeNotification((MBeanNotificationInfo)feature);
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
        try{
            tryStart(adapter, Collections.<String, String>emptyMap());
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
            processConfiguration(new SafeConsumer<AgentConfiguration>() {
                @Override
                public void accept(final AgentConfiguration config) {
                    final ManagedResourceConfiguration testResource =
                            config.getManagedResources().get(TEST_RESOURCE_NAME);
                    assertNotNull(testResource);
                    assertNotNull(testResource.getElements(AttributeConfiguration.class).remove("1.0"));
                    assertNotNull(testResource.getElements(AttributeConfiguration.class).remove("2.0"));
                    assertNotNull(testResource.getElements(AttributeConfiguration.class).remove("3.0"));
                    assertNotNull(testResource.getElements(AttributeConfiguration.class).remove("4.0"));
                    assertNotNull(testResource.getElements(EventConfiguration.class).remove("19.1"));
                }
            }, true);
            Thread.sleep(2000);
            assertEquals(5, adapter.getAttributes().size());
            assertEquals(1, adapter.getNotifications().size());
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
