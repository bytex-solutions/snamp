package com.bytex.snamp.testing.adapters.decanter;

import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.bytex.snamp.testing.connectors.jmx.TestOpenMBean;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.testing.connectors.jmx.TestOpenMBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.DECANTER_ADAPTER)
public class JmxToDecanterTest extends AbstractJmxConnectorTest<TestOpenMBean> implements EventConstants {
    private static final class DecanterEventListener extends SynchronizationEvent<Event> implements EventHandler{
        private DecanterEventListener(){
            super(true);
        }

        @Override
        public void handleEvent(final Event event) {
            fire(event);
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
            final Event ev = listener.getAwaitor().get(10, TimeUnit.SECONDS);
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
            final Future<Event> f = listener.getAwaitor();
            testAttribute("string", TypeToken.of(String.class), "Frank Underwood");
            final Event ev = f.get(10, TimeUnit.SECONDS);
            assertEquals("notice", ev.getProperty("severity"));
            assertTrue(ev.getProperty("data") instanceof Map<?, ?>);
            assertTrue(ev.containsProperty("message"));
            assertTrue(ev.containsProperty("sequenceNumber"));
        }finally {
            handler.unregister();
        }
    }

    @Override
    protected void fillAdapters(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        adapters.getOrAdd("decanter-adapter").setAdapterName("decanter");
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
