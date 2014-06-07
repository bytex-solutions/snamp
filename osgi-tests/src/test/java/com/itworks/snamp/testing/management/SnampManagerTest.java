package com.itworks.snamp.testing.management;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.itworks.snamp.connectors.DiscoveryService;
import com.itworks.snamp.licensing.LicensingDescriptionService;
import com.itworks.snamp.management.Maintainable;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestManagementBean;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.Factory;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.itworks.snamp.testing.connectors.jmx.TestManagementBean.BEAN_NAME;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnampManagerTest extends AbstractJmxConnectorTest<TestManagementBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";

    public SnampManagerTest() throws MalformedObjectNameException {
        super(new TestManagementBean(), new ObjectName(TestManagementBean.BEAN_NAME),
                SnampArtifact.MONITORING.getReference(),
                SnampArtifact.SNMP4J.getReference(),
                SnampArtifact.SNMP_ADAPTER.getReference());
    }

    @Test
    public void additionalComponentsTest(){
        final ServiceReference<SnampManager> managerRef = getTestBundleContext().getServiceReference(SnampManager.class);
        assertNotNull(managerRef);
        try{
            final SnampManager manager = getTestBundleContext().getService(managerRef);
            final Collection<SnampComponentDescriptor> components = manager.getInstalledComponents();
            assertFalse(components.isEmpty());
            for(final SnampComponentDescriptor c: components){
                assertFalse(c.getName(null).isEmpty());
                assertFalse(c.getDescription(null).isEmpty());
                assertNotNull(c.getVersion());
            }
        }
        finally {
            getTestBundleContext().ungetService(managerRef);
        }
    }

    private void testSnmpAdapterDescriptor(final SnampComponentDescriptor descriptor){
        assertEquals(new Version(1, 0, 0), descriptor.getVersion());
        assertFalse(descriptor.getDescription(Locale.getDefault()).isEmpty());
        descriptor.invokeManagementService(LicensingDescriptionService.class, new Closure<LicensingDescriptionService>() {
            @Override
            public void execute(final LicensingDescriptionService input) {
                assertFalse(input.getLimitations().isEmpty());
            }
        });
        descriptor.invokeManagementService(ConfigurationEntityDescriptionProvider.class, new Closure<ConfigurationEntityDescriptionProvider>() {
            @Override
            public void execute(final ConfigurationEntityDescriptionProvider input) {
                assertNotNull(input.getDescription(AgentConfiguration.ResourceAdapterConfiguration.class));
            }
        });
    }

    @Test
    public void adaptersManagementTest(){
        final ServiceReference<SnampManager> managerRef = getTestBundleContext().getServiceReference(SnampManager.class);
        assertNotNull(managerRef);
        try{
            final SnampManager manager = getTestBundleContext().getService(managerRef);
            final Collection<SnampComponentDescriptor> adapters = manager.getInstalledResourceAdapters();
            assertFalse(adapters.isEmpty());
            boolean snmpAdapterDiscovered = false;
            for(final SnampComponentDescriptor adapter: adapters)
                if(Objects.equals("SNMP Resource Adapter", adapter.getName(null))){
                    testSnmpAdapterDescriptor(adapter);
                    snmpAdapterDiscovered = true;
                }
            assertTrue(snmpAdapterDiscovered);
        }
        finally {
            getTestBundleContext().ungetService(managerRef);
        }
    }

    private static void testJmxConnectorDescriptor(final SnampComponentDescriptor descriptor){
        assertEquals(new Version(1, 0, 0), descriptor.getVersion());
        assertFalse(descriptor.getDescription(Locale.getDefault()).isEmpty());
        descriptor.invokeManagementService(DiscoveryService.class, new Closure<DiscoveryService>() {
            @Override
            public void execute(final DiscoveryService input) {
                assertNotNull(input);
            }
        });
        descriptor.invokeManagementService(LicensingDescriptionService.class, new Closure<LicensingDescriptionService>() {
            @Override
            public void execute(final LicensingDescriptionService input) {
                assertNotNull(input);
            }
        });
        descriptor.invokeManagementService(Maintainable.class, new Closure<Maintainable>() {
            @Override
            public void execute(final Maintainable input) {
                assertNotNull(input);
            }
        });
    }

    @Test
    public void connectorsManagementTest(){
        final ServiceReference<SnampManager> managerRef = getTestBundleContext().getServiceReference(SnampManager.class);
        assertNotNull(managerRef);
        try{
            final SnampManager manager = getTestBundleContext().getService(managerRef);
            final Collection<SnampComponentDescriptor> connectors = manager.getInstalledResourceConnectors();
            assertFalse(connectors.isEmpty());
            boolean jmxConnectorDiscovered = false;
            for(final SnampComponentDescriptor descriptor: connectors){
                assertEquals(Bundle.ACTIVE, descriptor.getState());
                if(Objects.equals(descriptor.getName(null), "JMX Connector")){
                    jmxConnectorDiscovered = true;
                    testJmxConnectorDescriptor(descriptor);
                }
            }
            assertTrue(jmxConnectorDiscovered);
        }
        finally {
            getTestBundleContext().ungetService(managerRef);
        }
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        AbstractResourceAdapterActivator.startResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
    }

    @Override
    protected void fillAdapters(final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapters, final Factory<AgentConfiguration.ResourceAdapterConfiguration> adapterFactory) {
        final AgentConfiguration.ResourceAdapterConfiguration snmpAdapter = adapterFactory.create();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getHostingParams().put("port", SNMP_PORT);
        snmpAdapter.getHostingParams().put("host", SNMP_HOST);
        snmpAdapter.getHostingParams().put("socketTimeout", "5000");
        adapters.put("test-snmp", snmpAdapter);
    }

    @Override
    protected void fillAttributes(final Map<String, AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributes, final Factory<AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration> attributeFactory) {
        AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration attribute = attributeFactory.create();
        attribute.setAttributeName("string");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.1.0");
        attributes.put("1.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("boolean");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.2.0");
        attributes.put("2.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("int32");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.3.0");
        attributes.put("3.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("bigint");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.4.0");
        attributes.put("4.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("array");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.5.1");
        attributes.put("5.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("dictionary");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.6.1");
        attributes.put("6.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("table");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.7.1");
        attributes.put("7.1", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("float");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("oid", "1.1.8.0");
        attributes.put("8.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        attribute.getParameters().put("oid", "1.1.9.0");
        attributes.put("9.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903-human-readable");
        attribute.getParameters().put("oid", "1.1.10.0");
        attributes.put("10.0", attribute);

        attribute = attributeFactory.create();
        attribute.setAttributeName("date");
        attribute.getParameters().put("objectName", BEAN_NAME);
        attribute.getParameters().put("displayFormat", "rfc1903");
        attribute.getParameters().put("oid", "1.1.11.0");
        attributes.put("11.0", attribute);
    }
}
