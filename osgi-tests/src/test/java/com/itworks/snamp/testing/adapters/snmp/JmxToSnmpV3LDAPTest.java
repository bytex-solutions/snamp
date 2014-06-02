package com.itworks.snamp.testing.adapters.snmp;

import com.itworks.snamp.adapters.AbstractResourceAdapterActivator;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.SnampArtifact;
import com.itworks.snamp.testing.connectors.jmx.AbstractJmxConnectorTest;
import com.itworks.snamp.testing.connectors.jmx.TestManagementBean;
import org.apache.commons.collections4.Factory;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.OID;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static com.itworks.snamp.testing.connectors.jmx.TestManagementBean.BEAN_NAME;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ExamReactorStrategy(PerMethod.class)
public final class JmxToSnmpV3LDAPTest extends AbstractJmxConnectorTest<TestManagementBean> {
    private static final String ADAPTER_NAME = "snmp";
    private static final String SNMP_PORT = "3222";
    private static final String SNMP_HOST = "127.0.0.1";
    private static final String LDAP_ADMIN_USER = "uid=admin,ou=system";
    private static final String LDAP_ADMIN_PASSWORD = "1-2-3-4-5-password";
    private static final String LDAP_USER = "cn=Roman";
    private static EmbeddedADSVerTrunk ads;
    private static File workDir;
    private static final SnmpClient client = SnmpClientFactory.createSnmpV3("udp:" + SNMP_HOST + "/" + SNMP_PORT, LDAP_USER, SecurityLevel.authPriv);

    //ldapsearch -h 127.0.0.1 -p 10389 -w 1-2-3-4-5-password -D uid=admin,ou=system -b dc=ad,dc=microsoft,dc=com
    public JmxToSnmpV3LDAPTest() throws MalformedObjectNameException {
        super(new TestManagementBean(), new ObjectName(BEAN_NAME),
                SnampArtifact.SNMP4J.getReference(),
                SnampArtifact.SNMP_ADAPTER.getReference(),
                wrappedBundle(maven("org.apache.directory.server", "apacheds-all", "2.0.0-M16")).exports("org.apache.directory.*; version=2.0.0.16"));
    }

    @Test
    public final void testForStringProperty() throws IOException, BundleException {
        try {
            final String valueToCheck = "SETTED VALUE";
            final OID attributeId = new OID("1.1.1.0");
            client.writeAttribute(attributeId, valueToCheck, String.class);
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GET, attributeId, String.class));
            assertEquals(valueToCheck, client.readAttribute(ReadMethod.GETBULK, attributeId, String.class));
        }
        finally {
            AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        }
    }

    private static void delete(final File f) throws IOException {
        if (f.isDirectory())
            for (final File c : f.listFiles())
                delete(c);
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        workDir = new File( System.getProperty( "java.io.tmpdir" ) + "/server-work" );

        if (workDir.exists()) delete(workDir);
        workDir.mkdirs();
        // Create the server
        ads = new EmbeddedADSVerTrunk( workDir );

        // optionally we can start a server too
        ads.startServer();
        super.beforeStartTest(context);
    }

    @Override
    protected void afterStartTest(final BundleContext context) throws Exception {
        super.afterStartTest(context);
        AbstractResourceAdapterActivator.stopResourceAdapter(getTestBundleContext(), ADAPTER_NAME);
        AbstractResourceAdapterActivator.startResourceConnector(getTestBundleContext(), ADAPTER_NAME);
    }

    @Override
    protected void beforeCleanupTest(final BundleContext context) throws Exception {
        ads.stopServer();
        workDir.delete();
    }

    @Override
    protected void fillAdapters(final Map<String, AgentConfiguration.ResourceAdapterConfiguration> adapters, final Factory<AgentConfiguration.ResourceAdapterConfiguration> adapterFactory) {
        final AgentConfiguration.ResourceAdapterConfiguration snmpAdapter = adapterFactory.create();
        snmpAdapter.setAdapterName(ADAPTER_NAME);
        snmpAdapter.getHostingParams().put("port", SNMP_PORT);
        snmpAdapter.getHostingParams().put("host", SNMP_HOST);
        snmpAdapter.getHostingParams().put("socketTimeout", "5000");
        adapters.put("test-snmp", snmpAdapter);
        snmpAdapter.getHostingParams().put("ldap-uri", "ldap://127.0.0.1:" + EmbeddedADSVerTrunk.SERVER_PORT);
        snmpAdapter.getHostingParams().put("ldap-user", LDAP_ADMIN_USER);
        snmpAdapter.getHostingParams().put("ldap-password", LDAP_ADMIN_PASSWORD);
        snmpAdapter.getHostingParams().put("ldap-auth-protocol", "simple");
        snmpAdapter.getHostingParams().put("ldap-base-dn", "dc=ad,dc=microsoft,dc=com");
        snmpAdapter.getHostingParams().put("ldap-user-search-filter", String.format("(%s)", LDAP_USER));
        snmpAdapter.getHostingParams().put("ldap-groups", "(&(objectclass=domain)(objectclass=top))");
    }

    @Override
    protected void fillEvents(final Map<String, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> events, final Factory<AgentConfiguration.ManagedResourceConfiguration.EventConfiguration> eventFactory) {
        AgentConfiguration.ManagedResourceConfiguration.EventConfiguration event = eventFactory.create();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getParameters().put("severity", "notice");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-1");
        event.getParameters().put("oid", "1.1.19.1");
        events.put("19.1", event);

        event = eventFactory.create();
        event.setCategory("com.itworks.snamp.connectors.tests.jmx.testnotif");
        event.getParameters().put("severity", "panic");
        event.getParameters().put("objectName", BEAN_NAME);
        event.getParameters().put("receiverAddress", SNMP_HOST + "/" + client.getClientPort());
        event.getParameters().put("receiverName", "test-receiver-2");
        event.getParameters().put("oid", "1.1.20.1");
        events.put("20.1", event);
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
