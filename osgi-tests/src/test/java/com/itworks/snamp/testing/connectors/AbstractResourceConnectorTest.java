package com.itworks.snamp.testing.connectors;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeTokens;
import com.itworks.snamp.concurrent.Awaitor;
import com.itworks.snamp.concurrent.SpinWait;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.ConfigParameters;
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
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
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

    protected static ConfigParameters toConfigParameters(final Map<String, String> parameters) {
        return new ConfigParameters(new AgentConfiguration.ConfigurationEntity() {
            @Override
            public Map<String, String> getParameters() {
                return parameters;
            }
        });
    }

    protected static interface Equator<V>{
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

    protected static <V> Equator<V> arrayEquator(){
        return new Equator<V>() {
            @Override
            public boolean equate(final V value1, final V value2) {
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

    private static <K, V> boolean areEqual(final Map<K, V> value1, final Map<K, V> value2) {
        if(value1.size() == value2.size())
            for(final K key: value1.keySet()) {
                if (!value2.containsKey(key)) return false;
                if(!Objects.equals(value1.get(key), value2.get(key)))
                    return false;
            }
        else return false;
        return true;
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

    private void waitForConnector(final TimeSpan timeout) throws TimeoutException, InterruptedException {
        final Awaitor<ServiceReference<ManagedResourceConnector<?>>, ExceptionPlaceholder> awaitor = new SpinWait<ServiceReference<ManagedResourceConnector<?>>, ExceptionPlaceholder>() {
            @Override
            protected ServiceReference<ManagedResourceConnector<?>> get() {
                return ManagedResourceConnectorClient.getResourceConnector(getTestBundleContext(), TEST_RESOURCE_NAME);
            }
        };
        awaitor.await(timeout);
    }

    private void waitForNoConnector(final TimeSpan timeout) throws TimeoutException, InterruptedException {
        final Awaitor<Object, ExceptionPlaceholder> awaitor = new SpinWait<Object, ExceptionPlaceholder>() {
            @Override
            protected Object get() {
                return ManagedResourceConnectorClient.getResourceConnector(getTestBundleContext(), TEST_RESOURCE_NAME) != null ? null : new Object();
            }
        };
        awaitor.await(timeout);
    }

    protected final ManagedResourceConnector<?> getManagementConnector(final BundleContext context){
        final ServiceReference<ManagedResourceConnector<?>> connectorRef =
                ManagedResourceConnectorClient.getConnectors(context).get(TEST_RESOURCE_NAME);
        return connectorRef != null ? getTestBundleContext().getService(connectorRef) : null;
    }

    protected final ManagedResourceConnector<?> getManagementConnector(){
        return getManagementConnector(getTestBundleContext());
    }

    protected final boolean releaseManagementConnector(){
        return releaseManagementConnector(getTestBundleContext());
    }

    protected final boolean releaseManagementConnector(final BundleContext context){
        final ServiceReference<ManagedResourceConnector<?>> connectorRef =
                ManagedResourceConnectorClient.getConnectors(context).get(TEST_RESOURCE_NAME);
        return connectorRef != null && getTestBundleContext().ungetService(connectorRef);
    }

    @SuppressWarnings("UnusedParameters")
    protected void fillAttributes(final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory){

    }

    @SuppressWarnings("UnusedParameters")
    protected void fillEvents(final Map<String, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> events, final Supplier<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> eventFactory){

    }

    protected final void stopResourceConnector(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        try(final LogicalOperation ignored = ConnectorTestLogicalOperation.stopResourceConnector(connectorType, testName)) {
            ManagedResourceActivator.stopResourceConnector(context, connectorType);
            waitForNoConnector(TimeSpan.fromSeconds(10));
        }
    }

    protected final void startResourceConnector(final BundleContext context) throws BundleException, TimeoutException, InterruptedException {
        try (final LogicalOperation ignored = ConnectorTestLogicalOperation.startResourceConnector(connectorType, testName)) {
            ManagedResourceActivator.startResourceConnector(context, connectorType);
            waitForConnector(TimeSpan.fromSeconds(10));
        }
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
        fillAttributes(targetConfig.getElements(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class), new Supplier<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration>() {
            @Override
            public AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration get() {
                return targetConfig.newElement(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class);
            }
        });
        fillEvents(targetConfig.getElements(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class), new Supplier<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration>() {
            @Override
            public AgentConfiguration.ManagedResourceConfiguration.EventConfiguration get() {
                return targetConfig.newElement(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
            }
        });
        config.getManagedResources().put(TEST_RESOURCE_NAME, targetConfig);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final Map<String, String> attributeOptions,
                                           final boolean readOnlyTest) throws JMException, IOException {
        try{
            final AttributeSupport connector = getManagementConnector().queryObject(AttributeSupport.class);
            assertNotNull(connector);
            final MBeanAttributeInfo metadata = connector.connectAttribute(attributeID, attributeName, TimeSpan.INFINITE, toConfigParameters(attributeOptions));
            assertEquals(attributeID, metadata.getName());
            if(!readOnlyTest)
                connector.setAttribute(new Attribute(attributeID, attributeValue));
            final T newValue = TypeTokens.cast(connector.getAttribute(attributeID), attributeType);
            assertNotNull(newValue);
            assertTrue(comparator.equate(attributeValue, newValue));
            assertTrue(connector.disconnectAttribute(attributeID));
        }
        finally {
            releaseManagementConnector();
        }
    }

    protected final <T> void testAttribute(final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final Map<String, String> attributeOptions,
                                           final boolean readOnlyTest) throws JMException, IOException {
       testAttribute("ID", attributeName, attributeType, attributeValue, comparator, attributeOptions, readOnlyTest);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final boolean readOnlyTest) throws Exception {
        final Map<String, String> attributeOptions = readSnampConfiguration().
                getManagedResources().
                get(TEST_RESOURCE_NAME).getElements(AttributeConfiguration.class).get(attributeID).getParameters();
        assertNotNull(String.format("Attribute %s with postfix %s doesn't exist in configuration.", attributeName, attributeID), attributeOptions);
        testAttribute(attributeID, attributeName, attributeType, attributeValue, comparator, attributeOptions, readOnlyTest);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator) throws Exception {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, comparator, false);
    }

    protected final <T> void testAttribute(final String attributeID,
                                       final String attributeName,
                                       final TypeToken<T> attributeType,
                                       final T attributeValue) throws Exception {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, false);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final boolean readOnlyTest) throws Exception {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, AbstractResourceConnectorTest.<T>valueEquator(), readOnlyTest);
    }
}
