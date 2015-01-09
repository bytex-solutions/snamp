package com.itworks.snamp.testing.connectors.snmp;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.Repeater;
import com.itworks.snamp.concurrent.SynchronizationEvent;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescription;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.attributes.UnknownAttributeException;
import com.itworks.snamp.connectors.notifications.*;
import com.itworks.snamp.mapping.TypeLiterals;
import com.itworks.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Represents SNMPv2 connector test with local SNMP agent.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpV2ConnectorTest extends AbstractSnmpConnectorTest {
    private static final String HOST_NAME = "127.0.0.1";
    private static final int REMOTE_PORT = 1161;
    private static final int LOCAL_PORT = 44495;

    private static Map<String, String> getParameters(final int localPort) {
        return ImmutableMap.of("community", "public",
                "localAddress", "udp://127.0.0.1/" + localPort);
    }

    private static Map<String, String> getParameters() {
        return getParameters(LOCAL_PORT);
    }

    private final BaseAgent agent;

    public SnmpV2ConnectorTest() {
        super(HOST_NAME, REMOTE_PORT, getParameters());
        agent = new BaseAgent(new File("conf.agent"), null,
                new CommandProcessor(
                        new OctetString(MPv3.createLocalEngineID()))) {

            private boolean coldStart = true;
            private Repeater notifSender;

            @Override
            protected void registerManagedObjects() {
                try {
                    server.register(new MOScalar<>(new OID("1.6.1.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new OctetString("Hello, world!")), null);
                    server.register(new MOScalar<>(new OID("1.6.2.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new Integer32(0)), null);
                    server.register(new MOScalar<>(new OID("1.6.3.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new UnsignedInteger32(0L)), null);
                    server.register(new MOScalar<>(new OID("1.6.4.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new TimeTicks(642584974L)), null);
                    server.register(new MOScalar<>(new OID("1.6.5.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new Counter32(0L)), null);
                    server.register(new MOScalar<>(new OID("1.6.6.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new Counter64(0L)), null);
                    server.register(new MOScalar<>(new OID("1.6.7.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new Gauge32(0L)), null);
                    server.register(new MOScalar<>(new OID("1.6.8.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new OID("1.10.10.0")), null);
                    server.register(new MOScalar<>(new OID("1.6.9.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new IpAddress("127.0.0.1")), null);
                    server.register(new MOScalar<>(new OID("1.6.10.0"),
                            new MOAccessImpl(MOAccessImpl.ACCESSIBLE_FOR_READ_WRITE),
                            new Opaque(new byte[]{1, 2, 3, 4})), null);
                } catch (final DuplicateRegistrationException e) {
                    fail(e.getMessage());
                }
            }

            protected void initTransportMappings() throws IOException {
                final TransportMappings mappings = TransportMappings.getInstance();
                try {
                    TransportMapping<?> tm = mappings.createTransportMapping(GenericAddress.parse(String.format("%s/%s", HOST_NAME, REMOTE_PORT)));
                    if (tm instanceof DefaultUdpTransportMapping)
                        ((DefaultUdpTransportMapping) tm).setSocketTimeout(5000);
                    transportMappings = new TransportMapping[]{tm};
                } catch (final RuntimeException e) {
                    throw new IOException(String.format("Unable to create SNMP transport for %s/%s address.", HOST_NAME, REMOTE_PORT), e);
                }
            }

            @Override
            protected void unregisterManagedObjects() {
            }

            @Override
            protected void addUsmUser(final USM usm) {

            }

            @Override
            protected void addNotificationTargets(final SnmpTargetMIB targetMIB, final SnmpNotificationMIB notificationMIB) {
                final OctetString NOTIFICATION_SETTINGS_TAG = new OctetString("NOTIF_TAG");
                targetMIB.addDefaultTDomains();
                targetMIB.addTargetAddress(new OctetString("test"),
                        TransportDomains.transportDomainUdpIpv4,
                        new OctetString(new UdpAddress(HOST_NAME + "/" + LOCAL_PORT).getValue()),
                        3000,
                        2,
                        new OctetString("notify"),
                        NOTIFICATION_SETTINGS_TAG,
                        StorageType.nonVolatile);
                targetMIB.addTargetParams(NOTIFICATION_SETTINGS_TAG,
                        MessageProcessingModel.MPv2c,
                        SecurityModel.SECURITY_MODEL_SNMPv2c,
                        new OctetString("cpublic"),
                        SecurityLevel.AUTH_PRIV,
                        StorageType.permanent);
                notificationMIB.addNotifyEntry(new OctetString("default"),
                        new OctetString("notify"),
                        SnmpNotificationMIB.SnmpNotifyTypeEnum.trap,
                        StorageType.permanent);
            }

            @Override
            protected void addViews(final VacmMIB vacm) {
                vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(
                                "cpublic"), new OctetString("v1v2group"),
                        StorageType.nonVolatile);

                vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                        SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV,
                        MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView"),
                        new OctetString("fullWriteView"), new OctetString(
                                "fullNotifyView"), StorageType.nonVolatile);

                vacm.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.6"),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
                vacm.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1.6"),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
                vacm.addViewTreeFamily(new OctetString("fullNotifyView"), new OID("1.7"),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void addCommunities(final SnmpCommunityMIB communityMIB) {
                final Variable[] com2sec = new Variable[]{new OctetString("public"), // community
                        // name
                        new OctetString("cpublic"), // security name
                        agent.getContextEngineID(), // local engine ID
                        new OctetString("public"), // default context name
                        new OctetString(), // transport tag
                        new Integer32(StorageType.nonVolatile), // storage type
                        new Integer32(RowStatus.active) // row status
                };
                final MOTableRow row = communityMIB.getSnmpCommunityEntry().createRow(
                        new OctetString("public2public").toSubIndex(true), com2sec);
                communityMIB.getSnmpCommunityEntry().addRow(row);
            }

            /**
             * Stops the agent by closing the SNMP session and associated transport
             * mappings.
             *
             * @since 1.1
             */
            @Override
            public void stop() {
                if (notifSender != null)
                    try {
                        notifSender.stop(TimeSpan.fromSeconds(1));
                    } catch (final Exception e) {
                        fail(e.getMessage());
                    }
                notifSender = null;
                super.stop();

            }

            @Override
            public void init() throws IOException {
                super.init();
                if (coldStart) getServer().addContext(new OctetString("public"));
                finishInit();
                run();
                if (coldStart) sendColdStartNotification();
                coldStart = false;
                notifSender = new Repeater(TimeSpan.fromSeconds(1)) {
                    @Override
                    protected void doAction() {
                        final VariableBinding[] bindings = {
                                new VariableBinding(new OID("1.7.1.1.0"), new OctetString("Hello, world!")),
                                new VariableBinding(new OID("1.7.1.2.0"), new Integer32(42))
                        };
                        if (notificationOriginator != null)
                            notificationOriginator.notify(new OctetString("public"), new OID("1.7.1"), bindings);
                    }
                };
                notifSender.run();
            }
        };
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        agent.init();
    }

    @Test
    public void notificationTest() throws TimeoutException, InterruptedException, NotificationSupportException, UnknownSubscriptionException {
        try {
            final ManagedResourceConnector<?> connector = getManagementConnector();
            final NotificationSupport notifications = connector.queryObject(NotificationSupport.class);
            assertNotNull(notifications);
            final String LIST_ID = "snmp-notif";
            final NotificationMetadata metadata = notifications.enableNotifications(LIST_ID, "1.7.1", new HashMap<String, String>(1) {{
                put("messageTemplate", "{1.0} - {2.0}");
            }});
            assertNotNull(metadata);
            final SynchronizationEvent<Notification> trap = new SynchronizationEvent<>(false);
            notifications.subscribe("123", new NotificationListener() {
                @Override
                public boolean handle(final String listId, final Notification n) {
                    return trap.fire(n);
                }
            }, false);
            //obtain client addresses
            final Address[] addresses = connector.queryObject(Address[].class);
            assertNotNull(addresses);
            assertEquals(1, addresses.length);
            assertTrue(addresses[0] instanceof UdpAddress);
            try {
                final Notification n = trap.getAwaitor().await(TimeSpan.fromSeconds(6));
                assertNotNull(n);
                assertEquals("Hello, world! - 42", n.getMessage());
                assertEquals(0L, n.getSequenceNumber());
                assertNull(n.getAttachment());
            } finally {
                assertTrue(notifications.unsubscribe("123"));
            }
            assertTrue(notifications.disableNotifications(LIST_ID));
        } finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void testForOpaqueProperty() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.10.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.OBJECT_ARRAY,
                new Byte[]{10, 20, 30, 40, 50},
                AbstractResourceConnectorTest.arrayEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForIpAddressProperty() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.9.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.OBJECT_ARRAY,
                ArrayUtils.boxArray(new IpAddress("192.168.0.1").toByteArray()),
                AbstractResourceConnectorTest.arrayEquator(),
                Collections.<String, String>emptyMap(),
                false);
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.STRING,
                "192.168.0.1",
                AbstractResourceConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }

    @Test
    public void testForOidProperty() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.8.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.OBJECT_ARRAY,
                ArrayUtils.boxArray(new OID("1.4.5.3.1").getValue()),
                AbstractResourceConnectorTest.arrayEquator(),
                Collections.<String, String>emptyMap(),
                false);
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.STRING,
                "1.4.5.3.1",
                AbstractResourceConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }

    @Test
    public void testForGauge32Property() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.7.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.LONG,
                42L,
                AbstractResourceConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForCounter64Property() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.6.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.LONG,
                42L,
                AbstractResourceConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForCounter32Property() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.5.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.LONG,
                42L,
                AbstractResourceConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForTimeTicksProperty() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.4.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.LONG,
                642584970L,
                AbstractResourceConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.STRING,
                new TimeTicks(642584974L).toString(),
                AbstractResourceConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }

    @Test
    public void testForUnsignedInteger32Property() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.3.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.LONG,
                42L,
                AbstractResourceConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForInteger32Property() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.2.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.INTEGER,
                42,
                AbstractResourceConnectorTest.<Integer>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForOctetStringProperty() throws TimeoutException, IOException, AttributeSupportException, UnknownAttributeException {
        final String ATTRIBUTE_ID = "1.6.1.0";
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.STRING,
                "Jack Ryan",
                AbstractResourceConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "text");
                }},
                false);
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.STRING,
                new OctetString("Java Enterprise Edition").toHexString(),
                AbstractResourceConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "hex");
                }},
                false);
        testAttribute(ATTRIBUTE_ID,
                TypeLiterals.OBJECT_ARRAY,
                new Byte[]{10, 20, 1, 4},
                AbstractResourceConnectorTest.arrayEquator(),
                new HashMap<String, String>(1) {{
                    put("snmpConversionFormat", "raw");
                }},
                false);
    }

    @Test
    public void discoveryServiceTest() {
        final Collection<AttributeConfiguration> attributes = ManagedResourceConnectorClient.discoverEntities(getTestBundleContext(),
                CONNECTOR_NAME,
                connectionString,
                getParameters(LOCAL_PORT + 1),
                AttributeConfiguration.class);
        assertNotNull(attributes);
        assertFalse(attributes.isEmpty());
    }

    @Test
    public void licensingServiceTest() {
        final Map<String, String> limitations = ManagedResourceConnectorClient.getLicenseLimitations(getTestBundleContext(), CONNECTOR_NAME, null);
        assertNotNull(limitations);
        assertFalse(limitations.isEmpty());
    }

    @Test
    public void configurationDescriptionServiceTest() {
        final ConfigurationEntityDescription<AttributeConfiguration> attributesConfig = ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(), CONNECTOR_NAME, AttributeConfiguration.class);
        assertNotNull(attributesConfig);
        final ConfigurationEntityDescription.ParameterDescription descr = attributesConfig.getParameterDescriptor("snmpConversionFormat");
        assertNotNull(descr);
        assertTrue(descr.getDescription(null).length() > 0);
        assertNotNull(ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(), CONNECTOR_NAME, EventConfiguration.class));
        assertNotNull(ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getTestBundleContext(), CONNECTOR_NAME, AgentConfiguration.ManagedResourceConfiguration.class));

    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        agent.stop();
    }
}
