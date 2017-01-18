package com.bytex.snamp.testing.connector.jmx;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.gateway.modeling.NotificationAccessor;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ConnectorTrackingTest extends AbstractJmxConnectorTest<TestOpenMBean> {


    private static final class TestGateway extends AbstractGateway {

        private final ArrayList<AttributeAccessor> attributes = new ArrayList<>();

        private final ArrayList<NotificationAccessor> notifications = new ArrayList<>();

        private TestGateway() {
            super("TestGateway");
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
        protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
            try {
                return Stream.concat(
                        ImmutableList.copyOf(attributes).stream(),
                        ImmutableList.copyOf(notifications).stream()
                );
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
    }

    public ConnectorTrackingTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(TestOpenMBean.BEAN_NAME));
    }

    private static boolean tryStart(final AbstractGateway gatewayInstance,
                                    final Map<String, String> parameters) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Method tryStartMethod = AbstractGateway.class.getDeclaredMethod("tryStart", Map.class);
        tryStartMethod.setAccessible(true);
        return (Boolean)tryStartMethod.invoke(gatewayInstance, parameters);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void simpleTrackingTest() throws Exception {
        final TestGateway gateway = new TestGateway();
        ManagedResourceConnectorClient.addResourceListener(getTestBundleContext(), gateway);
        try {
            tryStart(gateway, Collections.emptyMap());
            assertEquals(9, gateway.getAttributes().size());
            assertEquals(2, gateway.getNotifications().size());
            //now deactivate the resource connector. This action causes restarting of gateway instance
            stopResourceConnector(getTestBundleContext());
            assertTrue(gateway.getAttributes().isEmpty());
            assertTrue(gateway.getNotifications().isEmpty());
            //activate resource connector. This action causes registration of features
            startResourceConnector(getTestBundleContext());
            //...but this process is asynchronous
            Thread.sleep(2000);
            assertEquals(9, gateway.getAttributes().size());
            assertEquals(2, gateway.getNotifications().size());
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
            assertEquals(5, gateway.getAttributes().size());
            assertEquals(1, gateway.getNotifications().size());
        } finally {
            getTestBundleContext().removeServiceListener(gateway);
            gateway.close();
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
        attribute.setAlternativeName("string");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("2.0");
        attribute.setAlternativeName("boolean");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("3.0");
        attribute.setAlternativeName("int32");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("4.0");
        attribute.setAlternativeName("bigint");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("5.1");
        attribute.setAlternativeName("array");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("6.1");
        attribute.setAlternativeName("dictionary");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("7.1");
        attribute.setAlternativeName("table");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("8.0");
        attribute.setAlternativeName("float");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);

        attribute = attributes.getOrAdd("9.0");
        attribute.setAlternativeName("date");
        attribute.put("objectName", TestOpenMBean.BEAN_NAME);
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("19.1");
        event.setAlternativeName(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.put("severity", "notice");
        event.put("objectName", TestOpenMBean.BEAN_NAME);

        event = events.getOrAdd("20.1");
        event.setAlternativeName("com.bytex.snamp.connector.tests.impl.testnotif");
        event.put("severity", "panic");
        event.put("objectName", TestOpenMBean.BEAN_NAME);
    }
}
