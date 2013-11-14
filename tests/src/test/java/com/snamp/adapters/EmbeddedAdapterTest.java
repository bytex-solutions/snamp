package com.snamp.adapters;

import com.snamp.Temporary;
import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

import static com.snamp.connectors.util.NotificationUtils.SynchronizationListener;

import com.snamp.connectors.Notification;
import com.snamp.hosting.Agent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import javax.management.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 */
public final class EmbeddedAdapterTest extends JmxConnectorTest<TestManagementBean>{
    //@Rule
    //public final Timeout testTimeout = new Timeout(60000);

    public EmbeddedAdapterTest() throws MalformedObjectNameException {
        super(TestEmbeddedAdapter.NAME, new HashMap<String, String>(), new TestManagementBean(), new ObjectName(TestManagementBean.BEAN_NAME));
    }

    @Test
    public final void testForBigIntProperty(){
        final TestEmbeddedAdapter adapter = getTestContext().queryObject(TestEmbeddedAdapter.class);
        assertNotNull(adapter);
        adapter.setBigIntProperty(BigInteger.TEN);
        assertEquals(BigInteger.TEN, adapter.getBigIntProperty());
    }

    @Test
    public final void testForNotification() throws TimeoutException, InterruptedException {
        final TestEmbeddedAdapter adapter = getTestContext().queryObject(TestEmbeddedAdapter.class);
        final SynchronizationListener listener = new SynchronizationListener();
        final Object listenerId = adapter.addPropertyChangedListener(listener);
        assertNotNull(listenerId);
        //modify property for firing property-changed event
        adapter.setBigIntProperty(BigInteger.TEN);
        //wait for notification (25 sec)
        final Notification n = listener.getAwaitor().await(new TimeSpan(25, TimeUnit.SECONDS));
        assertNotNull(n);
        adapter.removePropertyChangedListener(listenerId);
        assertEquals(Notification.Severity.NOTICE, n.getSeverity());
        assertEquals("Property bigint is changed", n.getMessage());
        assertNotNull(n.getTimeStamp());
    }

    @Test
    public final void testForNotificationWithReconnection() throws TimeoutException, InterruptedException {
        final TestEmbeddedAdapter adapter = getTestContext().queryObject(TestEmbeddedAdapter.class);
        final SynchronizationListener listener = new SynchronizationListener();
        @Temporary
        final Object listenerId = adapter.addPropertyChangedListener(listener);
        assertNotNull(listenerId);
        //obtains connector from testing context
        final Agent.InstantiatedConnectors connectors = getTestContext().queryObject(Agent.InstantiatedConnectors.class);
        assertTrue(connectors.size() > 0);
        final ManagementConnector jmxConnector = connectors.get("jmx");
        assertNotNull(jmxConnector);
        //aborts the connection
        jmxConnector.doAction("simulateConnectionAbort", new Arguments(), TimeSpan.INFINITE);
        //modify property for firing property-changed event
        adapter.setBigIntProperty(BigInteger.TEN);
        //wait for notification (25 sec) after connection abort
        final Notification n = listener.getAwaitor().await(new TimeSpan(25, TimeUnit.SECONDS));
        assertNotNull(n);
    }

    @Override
    protected final void fillAttributes(final Map<String, AttributeConfiguration> attributes) {
        TestEmbeddedAdapter.fillAttributes(attributes);
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events) {
        TestEmbeddedAdapter.fillEvents(events);
    }
}
