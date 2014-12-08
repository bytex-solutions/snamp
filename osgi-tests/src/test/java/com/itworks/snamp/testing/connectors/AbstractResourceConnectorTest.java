package com.itworks.snamp.testing.connectors;

import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.mapping.RecordSet;
import com.itworks.snamp.mapping.RecordSetUtils;
import com.itworks.snamp.mapping.TypeConverter;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Arrays;
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

    protected static <V> Equator<V[]> arrayEquator(){
        return new Equator<V[]>() {
            @Override
            public boolean equate(final V[] value1, final V[] value2) {
                return Arrays.equals(value1, value2);
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

    @SuppressWarnings("unchecked")
    protected static Equator<RecordSet<String, ?>> namedRecordSetEquator(){
        return (Equator)recordSetEquator();
    }

    protected static <K, V> Equator<RecordSet<K, V>> recordSetEquator(){
        return new Equator<RecordSet<K, V>>() {
            @Override
            public boolean equate(final RecordSet<K, V> value1, final RecordSet<K, V> value2) {
                return areEqual(RecordSetUtils.toMap(value1), RecordSetUtils.toMap(value2));
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
                                            final String connectionString,
                                            final AbstractProvisionOption<?>... deps){
        this(connectorType, connectionString, Collections.<String, String>emptyMap(), deps);
    }

    protected AbstractResourceConnectorTest(final String connectorType,
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
    protected void fillAttributes(final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes, final Supplier<AttributeConfiguration> attributeFactory){

    }

    @SuppressWarnings("UnusedParameters")
    protected void fillEvents(final Map<String, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> events, final Supplier<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> eventFactory){

    }

    protected final void stopResourceConnector(final BundleContext context) throws BundleException{
        AbstractManagedResourceActivator.stopResourceConnector(context, connectorType);
    }

    protected final void startResourceConnector(final BundleContext context) throws BundleException{
        AbstractManagedResourceActivator.startResourceConnector(context, connectorType);
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
                                           final boolean readOnlyTest) throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        try{
            final AttributeSupport connector = getManagementConnector().queryObject(AttributeSupport.class);
            assertNotNull(connector);
            final AttributeMetadata metadata = connector.connectAttribute(attributeID, attributeName, attributeOptions);
            assertEquals(attributeName, metadata.getName());
            final TypeConverter<T> projection = metadata.getType().getProjection(attributeType);
            assertNotNull(projection);
            if(!readOnlyTest)
                connector.setAttribute(attributeID, TimeSpan.INFINITE, attributeValue);
            final T newValue = projection.convertFrom(connector.getAttribute(attributeID, TimeSpan.INFINITE));
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
                                           final boolean readOnlyTest) throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
       testAttribute("ID", attributeName, attributeType, attributeValue, comparator, attributeOptions, readOnlyTest);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator,
                                           final boolean readOnlyTest) throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
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
                                           final Equator<T> comparator) throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, comparator, false);
    }

    protected final <T> void testAttribute(final String attributeID,
                                       final String attributeName,
                                       final TypeToken<T> attributeType,
                                       final T attributeValue) throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, false);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final TypeToken<T> attributeType,
                                           final T attributeValue,
                                           final boolean readOnlyTest) throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, AbstractResourceConnectorTest.<T>valueEquator(), readOnlyTest);
    }
}
