package com.bytex.snamp.testing.connectors.groovy;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.connectors.notifications.SynchronizationListener;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public void dictionaryTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            final Object value = groovyConnector.getAttribute("dict");
            assertTrue(value instanceof CompositeData);
            assertEquals(67L, CompositeDataUtils.getLong((CompositeData) value, "key1", 0L));
            groovyConnector.setAttribute(new Attribute("dict", value));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void tableTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            final Object value = groovyConnector.getAttribute("table");
            assertTrue(value instanceof TabularData);
            assertEquals(2, ((TabularData)value).size());
            groovyConnector.setAttribute(new Attribute("table", value));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void notificationTest() throws Exception {
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try{
            final NotificationSupport notificationSupport = groovyConnector.queryObject(NotificationSupport.class);
            assertNotNull(notificationSupport);
            final SynchronizationListener listener = new SynchronizationListener("ev");
            final Future<Notification> awaitor = listener.getAwaitor();
            notificationSupport.addNotificationListener(listener, listener, null);
            final Notification notif = awaitor.get(2, TimeUnit.SECONDS);
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
    public void configurationDescriptionTest() {
        testConfigurationDescriptor(AgentConfiguration.ManagedResourceConfiguration.class, ImmutableSet.of(
                "initScript",
                "groovy.warnings",
                "groovy.source.encoding",
                "groovy.classpath",
                "groovy.output.verbose",
                "groovy.output.debug",
                "groovy.errors.tolerance"
        ));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }
}