package com.snamp.adapters;

import com.snamp.*;
import com.snamp.connectors.*;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

import static com.snamp.connectors.util.NotificationUtils.SynchronizationListener;

import static com.snamp.connectors.NotificationSupport.Notification;

import com.snamp.hosting.Agent;
import org.junit.Test;

import javax.management.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

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
    public final void loadTest() throws InterruptedException {
        final TestEmbeddedAdapter adapter = getTestContext().queryObject(TestEmbeddedAdapter.class);
        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final int maxTasks = 300;
        final CountDownLatch barrier = new CountDownLatch(maxTasks);
        for(int i = 0; i < maxTasks; i++){
            final BigInteger num = BigInteger.valueOf(i);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    adapter.setBigIntProperty(num);
                    barrier.countDown();
                }
            });
        }
        assertTrue(barrier.await(1, TimeUnit.MINUTES));
        boolean equals = false;
        for(int i = 0; i < maxTasks; i++)
            equals |= Objects.equals(BigInteger.valueOf(i), adapter.getBigIntProperty());
        assertTrue(equals);
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
        assertTrue(jmxConnector instanceof JmxMaintenanceSupport);
        ((JmxMaintenanceSupport)jmxConnector).simulateConnectionAbort();
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
