package com.itworks.snamp.testing.connectors;

import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import org.apache.commons.collections4.Factory;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.connectors.AbstractManagedResourceActivator.*;

/**
 * Represents an abstract class for all integration tests that checks management connectors.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractManagementConnectorTest extends AbstractSnampIntegrationTest {
    protected static interface Equator<V>{
        boolean equate(final V value1, final V value2);
    }

    protected static <V> Equator<V> valueEquator(){
        return new Equator<V>() {
            @Override
            public boolean equate(final V value1, final V value2) {
                return Objects.equals(value1, value1);
            }
        };
    }

    protected static <V> Equator<V[]> arrayEquator(){
        return new Equator<V[]>() {
            @Override
            public boolean equate(final V[] value1, final V[] value2) {
                return Arrays.equals(value1, value2);
            }
        };
    }

    protected static Equator<Map> mapEquator(){
        return new Equator<Map>() {
            @Override
            public boolean equate(final Map value1, final Map value2) {
                if(value1.size() == value2.size())
                    for(final Object key: value1.keySet())
                        if(!value2.containsKey(key)) return false;
                else return false;
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

    protected AbstractManagementConnectorTest(final String connectorType,
                                              final String connectionString,
                                              final AbstractProvisionOption<?>... deps){
        this(connectorType, connectionString, Collections.<String, String>emptyMap(), deps);
    }

    protected AbstractManagementConnectorTest(final String connectorType,
                                              final String connectionString,
                                              final Map<String, String> parameters,
                                              final AbstractProvisionOption<?>... deps){
        super(deps);
        this.connectorType = connectorType;
        this.connectionString = connectionString;
        this.connectorParameters = parameters;
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
    protected void fillAttributes(final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes, final Factory<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributeFactory){

    }

    @SuppressWarnings("UnusedParameters")
    protected void fillEvents(final Map<String, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> events, final Factory<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> eventFactory){

    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        //restart management connector bundle for updating SNAMP configuration
        stopResourceConnector(context, connectorType);
        startResourceConnector(context, connectorType);
    }

    protected void fillAdapters(final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapters, final Factory<AgentConfiguration.ResourceAdapterConfiguration> adapterFactory){

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
        fillAdapters(config.getResourceAdapters(), new Factory<AgentConfiguration.ResourceAdapterConfiguration>() {
            @Override
            public AgentConfiguration.ResourceAdapterConfiguration create() {
                return config.newConfigurationEntity(AgentConfiguration.ResourceAdapterConfiguration.class);
            }
        });
        targetConfig.setConnectionString(connectionString);
        targetConfig.setConnectionType(connectorType);
        fillAttributes(targetConfig.getElements(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class), new Factory<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration>() {
            @Override
            public AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration create() {
                return targetConfig.newElement(AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class);
            }
        });
        fillEvents(targetConfig.getElements(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class), new Factory<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration>() {
            @Override
            public AgentConfiguration.ManagedResourceConfiguration.EventConfiguration create() {
                return targetConfig.newElement(AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
            }
        });
        config.getManagedResources().put(TEST_RESOURCE_NAME, targetConfig);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final Class<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final Map<String, String> attributeOptions,
                                           final boolean readOnlyTest) throws TimeoutException, IOException {
        try{
            final AttributeSupport connector = getManagementConnector().queryObject(AttributeSupport.class);
            assertNotNull(connector);
            final AttributeMetadata metadata = connector.connectAttribute(attributeID, attributeName, attributeOptions);
            assertEquals(attributeName, metadata.getName());
            final TypeConverter<T> projection = metadata.getType().getProjection(attributeType);
            assertNotNull(projection);
            if(!readOnlyTest)
                assertTrue(connector.setAttribute(attributeID, TimeSpan.INFINITE, attributeValue));
            final T newValue = projection.convertFrom(connector.getAttribute(attributeID, TimeSpan.INFINITE, new Object()));
            assertNotNull(newValue);
            assertTrue(comparator.equate(attributeValue, newValue));
            assertTrue(connector.disconnectAttribute(attributeID));
        }
        finally {
            releaseManagementConnector();
        }
    }

    protected final <T> void testAttribute(final String attributeName,
                                           final Class<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final Map<String, String> attributeOptions,
                                           final boolean readOnlyTest) throws TimeoutException, IOException {
       testAttribute("ID", attributeName, attributeType, attributeValue, comparator, attributeOptions, readOnlyTest);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final Class<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final boolean readOnlyTest) throws TimeoutException, IOException {
        final Map<String, String> attributeOptions = readSnampConfiguration().
                getManagedResources().
                get(TEST_RESOURCE_NAME).getElements(AttributeConfiguration.class).get(attributeID).getParameters();
        assertNotNull(String.format("Attribute %s with postfix %s doesn't exist in configuration.", attributeName, attributeID), attributeOptions);
        testAttribute(attributeID, attributeName, attributeType, attributeValue, comparator, attributeOptions, readOnlyTest);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final Class<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator) throws TimeoutException, IOException {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, comparator, false);
    }

    protected final <T> void testAttribute(final String attributeID,
                                       final String attributeName,
                                       final Class<T> attributeType,
                                       final T attributeValue) throws TimeoutException, IOException {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, false);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final Class<T> attributeType,
                                           final T attributeValue,
                                           final boolean readOnlyTest) throws TimeoutException, IOException{
        testAttribute(attributeID, attributeName, attributeType, attributeValue, AbstractManagementConnectorTest.<T>valueEquator(), readOnlyTest);
    }
}
