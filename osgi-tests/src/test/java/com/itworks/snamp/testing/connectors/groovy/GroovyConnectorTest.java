package com.itworks.snamp.testing.connectors.groovy;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.Awaitor;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.notifications.NotificationSupport;
import com.itworks.snamp.connectors.notifications.SynchronizationListener;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.Notification;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class GroovyConnectorTest extends AbstractGroovyConnectorTest {

    @Test
    public void dummyAttributeTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            groovyConnector.setAttribute(new Attribute("dummy", 42));
            final Object value = groovyConnector.getAttribute("dummy");
            assertTrue(value instanceof Integer);
            assertEquals(42, value);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void jsonAttributeTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            final Object value = groovyConnector.getAttribute("json");
            assertTrue(value instanceof Integer);
            assertEquals(56, value);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void financeAttributeTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            final Object value = groovyConnector.getAttribute("finance");
            assertTrue(value instanceof Integer);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void notificationTest() throws JMException, TimeoutException, InterruptedException {
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try{
            final NotificationSupport notificationSupport = groovyConnector.queryObject(NotificationSupport.class);
            assertNotNull(notificationSupport);
            final SynchronizationListener listener = new SynchronizationListener("ev");
            final Awaitor<Notification, ExceptionPlaceholder> awaitor = listener.getAwaitor();
            notificationSupport.addNotificationListener(listener, listener, null);
            final Notification notif = awaitor.await(new TimeSpan(1000));
            assertNotNull(notif);
            assertEquals("Dummy event", notif.getMessage());
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void discoveryTest() throws Exception{
        final Collection<AttributeConfiguration> attributes = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(), CONNECTOR_TYPE,
                getConnectionString(),
                getDefaultConnectionParams(),
                AttributeConfiguration.class);
        assertEquals(1, attributes.size());
    }

    @Test
    public void configurationDescriptionTest(){
        final ConfigurationEntityDescription<?> description =
                ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(), CONNECTOR_TYPE, AgentConfiguration.ManagedResourceConfiguration.class);
        assertNotNull(description);
        final ConfigurationEntityDescription.ParameterDescription param =
                description.getParameterDescriptor("initScript");
        assertNotNull(param);
        assertFalse(param.getDescription(null).isEmpty());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }
}