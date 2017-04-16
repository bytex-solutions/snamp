package com.bytex.snamp.testing.connector;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.SpinWait;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.LoggingScope;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.After;
import org.junit.rules.TestName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.management.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents an abstract class for all integration tests that checks management connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class AbstractResourceConnectorTest extends AbstractSnampIntegrationTest {

    private static final class ConnectorTestLoggingScope extends LoggingScope {


        private ConnectorTestLoggingScope(final AbstractSnampIntegrationTest requester,
                                          final String operationName){
            super(requester, operationName);
        }

        private static ConnectorTestLoggingScope startResourceConnector(final AbstractSnampIntegrationTest test){
            return new ConnectorTestLoggingScope(test, "startResourceConnector");
        }

        private static ConnectorTestLoggingScope stopResourceConnector(final AbstractSnampIntegrationTest test){
            return new ConnectorTestLoggingScope(test, "stopResourceConnector");
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
    private final Stack<ManagedResourceConnectorClient> managedResourceConnectors;

    protected AbstractResourceConnectorTest(final String connectorType,
                                            final String connectionString){
        this(connectorType, connectionString, Collections.emptyMap());
    }

    protected AbstractResourceConnectorTest(final String connectorType,
                                            final String connectionString,
                                            final Map<String, String> parameters) {
        this.connectorType = connectorType;
        this.connectionString = connectionString;
        this.connectorParameters = parameters;
        managedResourceConnectors = new Stack<>();
    }

    public static void waitForConnector(final Duration timeout,
                                  final String resourceName,
                                  final BundleContext context) throws TimeoutException, InterruptedException {
        ManagedResourceConnectorClient.tryCreate(context, resourceName, timeout).ifPresent(SafeCloseable::close);
    }

    public static void waitForNoConnector(final Duration timeout,
                                           final String resourceName,
                                           final BundleContext context) throws TimeoutException, InterruptedException {
        SpinWait.until(() -> {
            final Optional<ManagedResourceConnectorClient> client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
            client.ifPresent(SafeCloseable::close);
            return client.isPresent();
        }, timeout);
    }

    protected final ManagedResourceConnector getManagementConnector() throws InstanceNotFoundException {
        final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(getTestBundleContext(), TEST_RESOURCE_NAME)
                .orElseThrow(() -> new InstanceNotFoundException(String.format("Resource %s doesn't exist", TEST_RESOURCE_NAME)));
        managedResourceConnectors.push(client);
        return client;
    }

    protected final void releaseManagementConnector() {
        managedResourceConnectors.pop().release(getTestBundleContext());
    }

    protected String getGroupName(){
        return "";
    }

    protected void fillSupervisors(final EntityMap<? extends SupervisorConfiguration> supervisors){
        
    }

    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes){

    }

    protected void fillEvents(final EntityMap<? extends EventConfiguration> events){

    }

    protected void fillOperations(final EntityMap<? extends OperationConfiguration> operations){

    }

    public final void stopResourceConnector(final TestName testName,
                                              final String connectorType,
                                             final String resourceName,
                                               final BundleContext context) throws TimeoutException, InterruptedException, BundleException, ExecutionException {
        try(final LoggingScope logger = ConnectorTestLoggingScope.stopResourceConnector(this)) {
            logger.info("Stopping in test " + testName.getMethodName());
            assertTrue(String.format("Connector %s is not deployed", connectorType), ManagedResourceActivator.disableConnector(context, connectorType));
            waitForNoConnector(Duration.ofSeconds(10), resourceName, context);
        }
    }

    protected final void stopResourceConnector(final BundleContext context) throws BundleException, TimeoutException, InterruptedException, ExecutionException {
        stopResourceConnector(testName, connectorType, TEST_RESOURCE_NAME, context);
    }

    public final void startResourceConnector(final TestName testName,
                                              final String connectorType,
                                              final String resourceName,
                                              final BundleContext context) throws TimeoutException, InterruptedException, BundleException, ExecutionException {
        try (final LoggingScope logger = ConnectorTestLoggingScope.startResourceConnector(this)) {
            logger.info("Starting in test " + testName.getMethodName());
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

    @After
    public final void verifyNoActiveConnectors() {
        assertTrue(managedResourceConnectors.empty());
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
        final ManagedResourceConfiguration targetConfig = config.getResources().getOrAdd(TEST_RESOURCE_NAME);
        targetConfig.putAll(connectorParameters);
        targetConfig.setGroupName(getGroupName());
        fillGateways(config.getGateways());
        fillSupervisors(config.getSupervisors());
        targetConfig.setConnectionString(connectionString);
        targetConfig.setType(connectorType);
        fillAttributes(targetConfig.getAttributes());
        fillEvents(targetConfig.getEvents());
        fillOperations(targetConfig.getOperations());
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
                                                                           final Duration timeout) throws E, InterruptedException, ExecutionException, TimeoutException, InstanceNotFoundException {
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
