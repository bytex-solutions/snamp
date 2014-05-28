package com.itworks.snamp.testing.connectors;

import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.attributes.AttributeMetadata;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import org.apache.commons.collections4.Equator;
import org.apache.commons.collections4.Factory;
import org.ops4j.pax.exam.options.AbstractProvisionOption;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
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
    private final String connectorType;
    private final String connectionString;
    protected static final String testManagementTarget = "test-target";

    protected AbstractManagementConnectorTest(final String connectorType,
                                              final String connectionString,
                                              final AbstractProvisionOption<?>... deps){
        super(deps);
        this.connectorType = connectorType;
        this.connectionString = connectionString;
    }

    protected final ManagedResourceConnector<?> getManagementConnector(final BundleContext context){
        final ServiceReference<ManagedResourceConnector<?>> connectorRef =
                getConnectors(context).get(testManagementTarget);
        return connectorRef != null ? getTestBundleContext().getService(connectorRef) : null;
    }

    protected final boolean releaseManagementConnector(final BundleContext context){
        final ServiceReference<ManagedResourceConnector<?>> connectorRef =
                getConnectors(context).get(testManagementTarget);
        return connectorRef != null && getTestBundleContext().ungetService(connectorRef);
    }

    protected String getAttributesNamespace(){
        return "";
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

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to modify.
     */
    @Override
    protected final void setupTestConfiguration(final AgentConfiguration config) {
        final AgentConfiguration.ManagedResourceConfiguration targetConfig =
                config.newConfigurationEntity(AgentConfiguration.ManagedResourceConfiguration.class);
        targetConfig.setConnectionString(connectionString);
        targetConfig.setConnectionType(connectorType);
        targetConfig.setNamespace(getAttributesNamespace());
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
        config.getManagedResources().put(testManagementTarget, targetConfig);
    }

    protected final <T> void testAttribute(final String attributeID,
                                           final String attributeName,
                                           final Class<T> attributeType,
                                           final T attributeValue,
                                           final Equator<T> comparator) throws TimeoutException, IOException {
        final Map<String, String> attributeOptions = readSnampConfiguration().
                getManagedResources().
                get(testManagementTarget).getElements(AttributeConfiguration.class).get(attributeID).getParameters();
        assertNotNull(String.format("Attribute %s with postfix %s doesn't exist in configuration.", attributeName, attributeID), attributeOptions);
        try{
            final AttributeSupport connector = getManagementConnector(getTestBundleContext()).queryObject(AttributeSupport.class);
            assertNotNull(connector);
            final AttributeMetadata metadata = connector.connectAttribute(attributeID, attributeName, attributeOptions);
            assertEquals(attributeName, metadata.getName());
            final TypeConverter<T> projection = metadata.getType().getProjection(attributeType);
            assertNotNull(projection);
            assertTrue(connector.setAttribute(attributeID, TimeSpan.INFINITE, attributeValue));
            final T newValue = projection.convertFrom(connector.getAttribute(attributeID, TimeSpan.INFINITE, new Object()));
            assertNotNull(newValue);
            assertTrue(comparator.equate(attributeValue, newValue));
            assertTrue(connector.disconnectAttribute(attributeID));
        }
        finally {
            releaseManagementConnector(getTestBundleContext());
        }
    }

    protected final <T> void testAttribute(final String attributeID,
                                       final String attributeName,
                                       final Class<T> attributeType,
                                       final T attributeValue) throws TimeoutException, IOException {
        testAttribute(attributeID, attributeName, attributeType, attributeValue, new Equator<T>() {
            @Override
            public boolean equate(final T o1, final T o2) {
                return Objects.equals(o1, o2);
            }

            @Override
            public int hash(final T o) {
                return System.identityHashCode(o);
            }
        });
    }
}
