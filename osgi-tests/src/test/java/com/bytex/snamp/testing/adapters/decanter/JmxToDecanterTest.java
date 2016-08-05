package com.bytex.snamp.testing.adapters.decanter;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.testing.BundleExceptionCallable;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.time.Duration;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.*;

import com.bytex.snamp.configuration.EntityMap;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@SnampDependencies(SnampFeature.DECANTER_ADAPTER)
public class JmxToDecanterTest extends AbstractJmxConnectorTest<TestOpenMBean> implements EventConstants {
    private static final String ADAPTER_NAME = "decanter";

    private static final class DecanterEventListener extends LinkedBlockingQueue<Event> implements EventHandler{
        private static final long serialVersionUID = -6332965879993615931L;

        @Override
        public void handleEvent(final Event event) {
            offer(event);
        }
    }

    public JmxToDecanterTest() throws MalformedObjectNameException {
        super(new TestOpenMBean(), new ObjectName(BEAN_NAME));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Test
    public void dictionaryAttributeTest() throws JMException, InterruptedException, ExecutionException, TimeoutException {
        final DecanterEventListener listener = new DecanterEventListener();
        final String[] topics = {"decanter/collect/" + TEST_RESOURCE_NAME + "/dictionary"};
        final Hashtable<String, String[]> ht = new Hashtable<>();
        ht.put(EVENT_TOPIC, topics);
        final ServiceRegistration<EventHandler> handler = getTestBundleContext().registerService(EventHandler.class, listener, ht);
        try{
            final Event ev = listener.poll(10, TimeUnit.SECONDS);
            assertNotNull(ev);
            assertEquals("dictionary", ev.getProperty("snampType"));
            assertEquals(CompositeData.class.getName(), ev.getProperty("javaType"));
            assertTrue(ev.getProperty("value") instanceof Map<?, ?>);
        }
        finally {
            handler.unregister();
        }
    }

    @Test
    public void notificationTest() throws JMException, InterruptedException, ExecutionException, TimeoutException {
        final DecanterEventListener listener = new DecanterEventListener();
        final String[] topics = {"decanter/collect/" + TEST_RESOURCE_NAME + "/jmx-attribute-change"};
        final Hashtable<String, String[]> ht = new Hashtable<>();
        ht.put(EVENT_TOPIC, topics);
        final ServiceRegistration<EventHandler> handler = getTestBundleContext().registerService(EventHandler.class, listener, ht);
        try {
            testAttribute("string", TypeToken.of(String.class), "Frank Underwood");
            final Event ev = listener.poll(10, TimeUnit.SECONDS);
            assertNotNull(ev);
            assertEquals("notice", ev.getProperty("severity"));
            assertTrue(ev.getProperty("data") instanceof Map<?, ?>);
            assertTrue(ev.containsProperty("message"));
            assertTrue(ev.containsProperty("sequenceNumber"));
        }finally {
            handler.unregister();
        }
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        super.beforeStartTest(context);
        beforeCleanupTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        startResourceConnector(context);
        syncWithAdapterStartedEvent(ADAPTER_NAME, (BundleExceptionCallable) () -> {
            ResourceAdapterActivator.startResourceAdapter(context, ADAPTER_NAME);
            return null;
        }, Duration.ofMinutes(4));
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(context, ADAPTER_NAME);
        stopResourceConnector(context);
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        adapters.getOrAdd("decanter-adapter").setAdapterName(ADAPTER_NAME);
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("string");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("array");
        attribute.getParameters().put("objectName", BEAN_NAME);

        attribute = attributes.getOrAdd("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("typeName", "dict");
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
    }
}
