package com.itworks.snamp.testing.connectors;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeTokens;
import com.itworks.snamp.concurrent.Awaitor;
import com.itworks.snamp.concurrent.SpinWait;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.OperationConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.rules.TestName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Represents an abstract class for all integration tests that checks management connectors.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractResourceConnectorTest extends AbstractSnampIntegrationTest {
    private static final String CONNECTOR_TYPE_PROPERTY = "connectorType";

    private static final class ConnectorTestLogicalOperation extends TestLogicalOperation{


        private ConnectorTestLogicalOperation(final String operationName,
                                     final String connectorType,
                                     final TestName testName){
            super(operationName, testName, CONNECTOR_TYPE_PROPERTY, connectorType);
        }

        private static ConnectorTestLogicalOperation startResourceConnector(final String connectorType, final TestName name){
            return new ConnectorTestLogicalOperation("startResourceConnector", connectorType, name);
        }

        private static ConnectorTestLogicalOperation stopResourceConnector(final String connectorType, final TestName name){
            return new ConnectorTestLogicalOperation("stopResourceConnector", connectorType, name);
        }
    }

    protected interface Equator<V>{
        boolean equate(final V value1, final V value2);
    }

    protected static <V> Equator<V> valueEquator(){
        return new Equator<V>() {
            @Override
            public boolean equate(final V value1, final V value2) {
                return Objects.equals(value1, value2);
            }
        };
    }

    protected static Equator arrayEquator(){
        return new Equator() {
            @Override
            public boolean equate(final Object value1, final Object value2) {
                return Objects.equals(value1.getClass().getComponentType(), value2.getClass().getComponentType()) &&
                        ArrayUtils.equals(value1, value2);
            }
        };
    }

    protected static <V> Equator<V> successEquator(){
        return new Equator<V>() {
            @Override
            public boolean equate(final V value1, final V value2) {
                return true;
            }
        };
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
        this(connectorType, connectionString, Collections.<String, String>emptyMap());
    }

    protected AbstractResourceConnectorTest(final String connectorType,
                                            final String connectionString,
                                            final Map<String, String> parameters){
        this.connectorType = connectorType;
        this.connectionString = connectionString;
        this.connectorParameters = parameters;
    }

    private static void waitForConnector(final TimeSpan timeout,
                                  final String resourceName,
                                  final BundleContext context) throws TimeoutException, InterruptedException {
        final Awaitor<ServiceReference<ManagedResourceConnector>, ExceptionPlaceholder> awaitor = new SpinWait<ServiceReference<ManagedResourceConnector>, ExceptionPlaceholder>() {
            @Override
            protected ServiceReference<ManagedResourceConnector> get() {
                return ManagedResourceConnectorClient.getResourceConnector(context, resourceName);
            }
        };
        awaitor.await(timeout);
    }

    private static void waitForNoConnector(final TimeSpan timeout,
                                           final String resourceName,
                                           final BundleContext context) throws TimeoutException, InterruptedException {
        final Awaitor<Object, ExceptionPlaceholder> awaitor = new SpinWait<Object, ExceptionPlaceholder>() {
            @Override
            protected Object get() {
                return ManagedResourceConnectorClient.getResourceConnector(context, resourceName) != null ? null : new Object();
            }
        };
        awaitor.await(timeout);
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



    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory){

    }

    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory){

    }

    protected void fillOperations(final Map<String, OperationConfiguration> operations, final Supplier<OperationConfiguration> operationFactory){

    }

    public static void stopResourceConnector(final TestName testName,
                                              final String connectorType,
                                             final String resourceName,
                                               final BundleContext context) throws TimeoutException, InterruptedException, BundleException {
        try(final LogicalOperation ignored = ConnectorTestLogicalOperation.stopResourceConnector(connectorType, testName)) {
            ManagedResourceActivator.stopResourceConnector(context, connectorType);
            waitForNoConnector(TimeSpan.fromSeconds(10), resourceName, context);
        }
    }

    protected final void stopResourceConnector(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        stopResourceConnector(testName, connectorType, TEST_RESOURCE_NAME, context);
    }

    public static void startResourceConnector(final TestName testName,
                                              final String connectorType,
                                              final String resourceName,
                                              final BundleContext context) throws TimeoutException, InterruptedException, BundleException {
        try (final LogicalOperation ignored = ConnectorTestLogicalOperation.startResourceConnector(connectorType, testName)) {
            ManagedResourceActivator.startResourceConnector(context, connectorType);
            waitForConnector(TimeSpan.fromSeconds(10000), resourceName, context);
        }
    }

    protected final void startResourceConnector(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
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

    protected void fillAdapters(final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapters, final Supplier<AgentConfiguration.ResourceAdapterConfiguration> adapterFactory){

    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to modify.
     */
    @Override
    protected final void setupTestConfiguration(final AgentConfiguration config) {
        final AgentConfiguration.ManagedResourceConfiguration targetConfig =
                config.newConfigurationEntity(AgentConfiguration.ManagedResourceConfiguration.class);
        targetConfig.getParameters().putAll(connectorParameters);
        fillAdapters(config.getResourceAdapters(), new Supplier<AgentConfiguration.ResourceAdapterConfiguration>() {
            @Override
            public AgentConfiguration.ResourceAdapterConfiguration get() {
                return config.newConfigurationEntity(AgentConfiguration.ResourceAdapterConfiguration.class);
            }
        });
        targetConfig.setConnectionString(connectionString);
        targetConfig.setConnectionType(connectorType);
        fillAttributes(targetConfig.getElements(AttributeConfiguration.class), new Supplier<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration>() {
            @Override
            public AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration get() {
                return targetConfig.newElement(AttributeConfiguration.class);
            }
        });
        fillEvents(targetConfig.getElements(EventConfiguration.class), new Supplier<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration>() {
            @Override
            public AgentConfiguration.ManagedResourceConfiguration.EventConfiguration get() {
                return targetConfig.newElement(EventConfiguration.class);
            }
        });
        fillOperations(targetConfig.getElements(OperationConfiguration.class), new Supplier<OperationConfiguration>() {
            @Override
            public OperationConfiguration get() {
                return targetConfig.newElement(OperationConfiguration.class);
            }
        });
        config.getManagedResources().put(TEST_RESOURCE_NAME, targetConfig);
    }

    protected final <T> void testAttribute(final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final boolean readOnlyTest) throws JMException {
        try{
            final AttributeSupport connector = getManagementConnector().queryObject(AttributeSupport.class);
            assertNotNull(connector);
            final MBeanAttributeInfo metadata = connector.getAttributeInfo(attributeName);
            assertEquals(attributeName, metadata.getName());
            if(!readOnlyTest)
                connector.setAttribute(new Attribute(attributeName, attributeValue));
            final T newValue = TypeTokens.cast(connector.getAttribute(attributeName), attributeType);
            assertNotNull(newValue);
            assertTrue(comparator.equate(attributeValue, newValue));
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
                AbstractResourceConnectorTest.<T>valueEquator(),
                false);
    }

    protected final <T> void testAttribute(final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final boolean readOnlyTest) throws JMException {
        testAttribute(attributeName, attributeType, attributeValue, AbstractResourceConnectorTest.<T>valueEquator(), readOnlyTest);
    }

    private static void testConfigurationDescriptor(final ConfigurationEntityDescription<?> description,
                                                      final Set<String> parameters){
        assertNotNull(description);
        int matches = 0;
        for (final String paramName : description) {
            final ConfigurationEntityDescription.ParameterDescription param =
                    description.getParameterDescriptor(paramName);
            assertNotNull(param);
            assertFalse("Invalid param description " + paramName, param.getDescription(null).isEmpty());
            if(parameters.contains(paramName))
                matches += 1;
        }
        if(matches != parameters.size())
            fail("Not all configuration parameters match to the expected list");
    }

    public static void testConfigurationDescriptor(final ConfigurationEntityDescription<?> description,
                                                    final String... parameters) {
        testConfigurationDescriptor(description, ImmutableSet.copyOf(parameters));
    }

    protected static <E extends AgentConfiguration.EntityConfiguration> void testConfigurationDescriptor(final BundleContext context,
                                                                                                          final String connectorType,
                                                                                                          final Class<E> entityType,
                                                                                                          final Set<String> parameters) {
        final ConfigurationEntityDescription<?> description = ManagedResourceConnectorClient.getConfigurationEntityDescriptor(context, connectorType, entityType);
        testConfigurationDescriptor(description, parameters);
    }

    protected final <E extends AgentConfiguration.EntityConfiguration> void testConfigurationDescriptor(final Class<E> entityType,
                                                                                                        final Set<String> parameters){
        testConfigurationDescriptor(getTestBundleContext(), connectorType, entityType, parameters);
    }
}
