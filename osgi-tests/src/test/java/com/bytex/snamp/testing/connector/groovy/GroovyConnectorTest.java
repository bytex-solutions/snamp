package com.bytex.snamp.testing.connector.groovy;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.operations.OperationSupport;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GroovyConnectorTest extends AbstractGroovyConnectorTest {

    @Test
    public void dummyAttributeTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            groovyConnector.setAttribute(new Attribute("DummyAttribute", 42));
            final Object value = groovyConnector.getAttribute("DummyAttribute");
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
            final Object value = groovyConnector.getAttribute("JsonAttribute");
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
            final Object value = groovyConnector.getAttribute("Yahoo");
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
            final Object value = groovyConnector.getAttribute("Dictionary");
            assertTrue(value instanceof CompositeData);
            assertEquals(67L, CompositeDataUtils.getLong((CompositeData) value, "key1", 0L));
            groovyConnector.setAttribute(new Attribute("Dictionary", value));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void tableTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            final Object value = groovyConnector.getAttribute("Table");
            assertTrue(value instanceof TabularData);
            assertEquals(2, ((TabularData)value).size());
            groovyConnector.setAttribute(new Attribute("Table", value));
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
            final Mailbox listener = MailboxFactory.newMailbox(n -> n.getType().equals("GroovyEvent"));
            notificationSupport.addNotificationListener(listener, listener, null);
            final Notification notif = listener.poll(2, TimeUnit.SECONDS);
            assertNotNull(notif);
            assertEquals("Dummy event", notif.getMessage());
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void operationTest() throws JMException {
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            final OperationSupport operations = groovyConnector.queryObject(OperationSupport.class);
            assertNotNull(operations);
            final Object value = operations.invoke("CustomOperation", new Long[]{38L, 90L}, new String[]{Long.class.getName(), Long.class.getName()});
            assertTrue(value instanceof Long);
            assertEquals(128L, value);
        } finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void discoveryTest() throws Exception{
        final Collection<AttributeConfiguration> attributes = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(), CONNECTOR_TYPE,
                getConnectionString(),
                ImmutableMap.of(),
                AttributeConfiguration.class);
        assertEquals(5, attributes.size());
    }

    @Test
    public void configurationDescriptionTest() {
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
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