package com.bytex.snamp.testing.connector;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.SpinWait;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.LogicalOperation;
import com.bytex.snamp.core.RichLogicalOperation;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.rules.TestName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import javax.management.Attribute;
import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.Notification;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Represents an abstract class for all integration tests that checks management connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractResourceConnectorTest extends AbstractSnampIntegrationTest {
    private static final String CONNECTOR_TYPE_PROPERTY = "connectorType";

    private static final class ConnectorTestLogicalOperation extends RichLogicalOperation{


        private ConnectorTestLogicalOperation(final String operationName,
                                     final String connectorType,
                                     final TestName testName){
            super(Logger.getLogger(testName.getMethodName()), operationName, ImmutableMap.of(CONNECTOR_TYPE_PROPERTY, connectorType));
        }

        private static ConnectorTestLogicalOperation startResourceConnector(final String connectorType,
                                                                            final TestName name){
            return new ConnectorTestLogicalOperation("startResourceConnector", connectorType, name);
        }

        private static ConnectorTestLogicalOperation stopResourceConnector(final String connectorType,
                                                                           final TestName name){
            return new ConnectorTestLogicalOperation("stopResourceConnector", connectorType, name);
        }
    }

    protected interface Equator<V>{
        boolean equate(final V value1, final V value2);
    }

    private final String connectorType;
    /**
     * Represents connection string.
     */
    protected final String connectionString;
    protected static final String TEST_RESOURCE_NAME = "test-target";
    private final Map<String, String> connectorParameters;

    protected AbstractResourceConnectorTest(final String connectorType,
                                            final String connectionString){
        this(connectorType, connectionString, Collections.emptyMap());
    }

    protected AbstractResourceConnectorTest(final String connectorType,
                                            final String connectionString,
                                            final Map<String, String> parameters){
        this.connectorType = connectorType;
        this.connectionString = connectionString;
        this.connectorParameters = parameters;
    }

    private static void waitForConnector(final Duration timeout,
                                  final String resourceName,
                                  final BundleContext context) throws TimeoutException, InterruptedException {
        SpinWait.spinUntilNull(context, resourceName, ManagedResourceConnectorClient::getResourceConnector, timeout);
    }

    private static void waitForNoConnector(final Duration timeout,
                                           final String resourceName,
                                           final BundleContext context) throws TimeoutException, InterruptedException {
        SpinWait.spinUntil(() -> ManagedResourceConnectorClient.getResourceConnector(context, resourceName) != null, timeout);
    }

    protected final ManagedResourceConnector getManagementConnector(final BundleContext context){
        final ServiceReference<ManagedResourceConnector> connectorRef =
                ManagedResourceConnectorClient.getConnectors(context).get(TEST_RESOURCE_NAME);
        return connectorRef != null ? getTestBundleContext().getService(connectorRef) : null;
    }

    protected final ManagedResourceConnector getManagementConnector(){
        return getManagementConnector(getTestBundleContext());
    }

    protected final boolean releaseManagementConnector(){
        return releaseManagementConnector(getTestBundleContext());
    }

    protected final boolean releaseManagementConnector(final BundleContext context){
        final ServiceReference<ManagedResourceConnector> connectorRef =
                ManagedResourceConnectorClient.getConnectors(context).get(TEST_RESOURCE_NAME);
        return connectorRef != null && context.ungetService(connectorRef);
    }



    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes){

    }

    protected void fillEvents(final EntityMap<? extends EventConfiguration> events){

    }

    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations){

    }

    public static void stopResourceConnector(final TestName testName,
                                              final String connectorType,
                                             final String resourceName,
                                               final BundleContext context) throws TimeoutException, InterruptedException, BundleException, ExecutionException {
        try(final LogicalOperation ignored = ConnectorTestLogicalOperation.stopResourceConnector(connectorType, testName)) {
            assertTrue(String.format("Connector %s is not deployed", connectorType), ManagedResourceActivator.disableConnector(context, connectorType));
            waitForNoConnector(Duration.ofSeconds(10), resourceName, context);
        }
    }

    protected final void stopResourceConnector(final BundleContext context) throws BundleException, TimeoutException, InterruptedException, ExecutionException {
        stopResourceConnector(testName, connectorType, TEST_RESOURCE_NAME, context);
    }

    public static void startResourceConnector(final TestName testName,
                                              final String connectorType,
                                              final String resourceName,
                                              final BundleContext context) throws TimeoutException, InterruptedException, BundleException, ExecutionException {
        try (final LogicalOperation ignored = ConnectorTestLogicalOperation.startResourceConnector(connectorType, testName)) {
            assertTrue(String.format("Connector %s is not deployed", connectorType), ManagedResourceActivator.enableConnector(context, connectorType));
            waitForConnector(Duration.ofSeconds(10), resourceName, context);
        }
    }

    protected final void startResourceConnector(final BundleContext context) throws BundleException, TimeoutException, InterruptedException, ExecutionException {
        startResourceConnector(testName,
                connectorType,
                TEST_RESOURCE_NAME,
                context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        //restart management connector bundle for updating SNAMP configuration
        stopResourceConnector(context);
        startResourceConnector(context);
    }

    protected void fillGateways(final EntityMap<? extends GatewayConfiguration> gateways){

    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to modify.
     */
    @Override
    protected final void setupTestConfiguration(final AgentConfiguration config) {
        final ManagedResourceConfiguration targetConfig =
                config.getEntities(ManagedResourceConfiguration.class).getOrAdd(TEST_RESOURCE_NAME);
        targetConfig.getParameters().putAll(connectorParameters);
        fillGateways(config.getEntities(GatewayConfiguration.class));
        targetConfig.setConnectionString(connectionString);
        targetConfig.setType(connectorType);
        fillAttributes(targetConfig.getFeatures(AttributeConfiguration.class));
        fillEvents(targetConfig.getFeatures(EventConfiguration.class));
        fillOperations(targetConfig.getFeatures(OperationConfiguration.class));
    }

    protected final <T> void testAttribute(final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final boolean readOnlyTest) throws JMException {
        final DynamicMBean connector = getManagementConnector();
        try{
            if(!readOnlyTest)
                connector.setAttribute(new Attribute(attributeName, attributeValue));
            final T newValue = Convert.toTypeToken(connector.getAttribute(attributeName), attributeType);
            assertNotNull(newValue);
            assertTrue(String.format("%s != %s", attributeValue, newValue), comparator.equate(attributeValue, newValue));
        }
        finally {
            releaseManagementConnector();
        }
    }

    protected final <T> void testAttribute(final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator) throws JMException {
        testAttribute(attributeName, attributeType, attributeValue, comparator, false);
    }

    protected final <T> void testAttribute(final String attributeName,
                                       final TypeToken<T> attributeType,
                                       final T attributeValue) throws JMException {
        testAttribute(attributeName, attributeType, attributeValue,
                Objects::equals,
                false);
    }

    protected final <T> void testAttribute(final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final boolean readOnlyTest) throws JMException {
        testAttribute(attributeName, attributeType, attributeValue, Objects::equals, readOnlyTest);
    }

    private static void testConfigurationDescriptor(final ConfigurationEntityDescription<?> description,
                                                      final Set<String> parameters){
        assertNotNull(description);
        int matches = 0;
        for (final String paramName : description) {
            final ConfigurationEntityDescription.ParameterDescription param =
                    description.getParameterDescriptor(paramName);
            assertNotNull(param);
            assertFalse("Invalid param description " + paramName, param.toString(null).isEmpty());
            if(parameters.contains(paramName))
                matches += 1;
        }
        if(matches != parameters.size())
            fail("Not all configuration parameters match to the expected list. Actual count = " +
                    matches +
                    ". Expected count = " +
                    parameters.size());
    }

    public static void testConfigurationDescriptor(final ConfigurationEntityDescription<?> description,
                                                    final String... parameters) {
        testConfigurationDescriptor(description, ImmutableSet.copyOf(parameters));
    }

    protected static <E extends EntityConfiguration> void testConfigurationDescriptor(final BundleContext context,
                                                                                      final String connectorType,
                                                                                      final Class<E> entityType,
                                                                                      final Set<String> parameters) {
        final ConfigurationEntityDescription<?> description = ManagedResourceConnectorClient.getConfigurationEntityDescriptor(context, connectorType, entityType);
        testConfigurationDescriptor(description, parameters);
    }

    protected final <E extends EntityConfiguration> void testConfigurationDescriptor(final Class<E> entityType,
                                                                                     final Set<String> parameters){
        testConfigurationDescriptor(getTestBundleContext(), connectorType, entityType, parameters);
    }

    protected final <E extends Throwable> Notification waitForNotification(final String listID,
                                                     final Acceptor<ManagedResourceConnector, E> sender,
                                                                           final Duration timeout) throws E, InterruptedException, ExecutionException, TimeoutException {
        final Mailbox listener = MailboxFactory.newMailbox(listID);
        final ManagedResourceConnector connector = getManagementConnector();
        try {
            connector
                    .queryObject(NotificationSupport.class)
                    .addNotificationListener(listener, listener, null);
            sender.accept(connector);
        } finally {
            releaseManagementConnector();
        }
        return listener.poll(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }
}
