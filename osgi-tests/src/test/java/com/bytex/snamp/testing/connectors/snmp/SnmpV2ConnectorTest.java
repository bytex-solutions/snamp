package com.bytex.snamp.testing.connectors.snmp;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.agent.mo.MOScalar;
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

import javax.management.JMException;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityMap;

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
                final SnmpCommunityMIB.SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(
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
                        notifSender.stop(TimeSpan.ofSeconds(1));
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
                notifSender = new Repeater(TimeSpan.ofSeconds(1)) {
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
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        agent.init();
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("snmp-notif");
        setFeatureName(event, "1.7.1");
        event.getParameters().put("messageTemplate", "{1.0} - {2.0}");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attribute = attributes.getOrAdd("opaqueAttr");
        setFeatureName(attribute, "1.6.10.0");

        attribute = attributes.getOrAdd("ipAddressAsByte");
        setFeatureName(attribute, "1.6.9.0");

        attribute = attributes.getOrAdd("ipAddressAsString");
        setFeatureName(attribute, "1.6.9.0");
        attribute.getParameters().put("snmpConversionFormat", "text");

        attribute = attributes.getOrAdd("oidAsIntArray");
        setFeatureName(attribute, "1.6.8.0");

        attribute = attributes.getOrAdd("oidAsString");
        setFeatureName(attribute, "1.6.8.0");
        attribute.getParameters().put("snmpConversionFormat", "text");

        attribute = attributes.getOrAdd("gauge");
        setFeatureName(attribute, "1.6.7.0");

        attribute = attributes.getOrAdd("counter64");
        setFeatureName(attribute, "1.6.6.0");

        attribute = attributes.getOrAdd("counter32");
        setFeatureName(attribute, "1.6.5.0");

        attribute = attributes.getOrAdd("timeTicksAsLong");
        setFeatureName(attribute, "1.6.4.0");

        attribute = attributes.getOrAdd("timeTicksAsString");
        setFeatureName(attribute, "1.6.4.0");
        attribute.getParameters().put("snmpConversionFormat", "text");

        attribute = attributes.getOrAdd("uint32");
        setFeatureName(attribute, "1.6.3.0");

        attribute = attributes.getOrAdd("int32");
        setFeatureName(attribute, "1.6.2.0");

        attribute = attributes.getOrAdd("octetstring");
        setFeatureName(attribute, "1.6.1.0");
        attribute.getParameters().put("snmpConversionFormat", "text");

        attribute = attributes.getOrAdd("hexstring");
        setFeatureName(attribute, "1.6.1.0");
        attribute.getParameters().put("snmpConversionFormat", "hex");

        attribute = attributes.getOrAdd("octetstringAsByteArray");
        setFeatureName(attribute, "1.6.1.0");
        attribute.getParameters().put("snmpConversionFormat", "raw");
    }

    @Test
    public void notificationTest() throws Exception {
        final ManagedResourceConnector connector = getManagementConnector();
        try {
            final NotificationSupport notifications = connector.queryObject(NotificationSupport.class);
            assertNotNull(notifications);
            assertNotNull(notifications.getNotificationInfo("snmp-notif"));
            final SynchronizationEvent<Notification> trap = new SynchronizationEvent<>(false);
            notifications.addNotificationListener(new NotificationListener() {
                @Override
                public void handleNotification(final Notification notification, final Object handback) {
                    trap.fire(notification);
                }
            }, null, null);
            //obtain client addresses
            final Address[] addresses = connector.queryObject(Address[].class);
            assertNotNull(addresses);
            assertEquals(1, addresses.length);
            assertTrue(addresses[0] instanceof UdpAddress);
            final Notification n = trap.getAwaitor().get(6, TimeUnit.SECONDS);
            assertNotNull(n);
            assertEquals("Hello, world! - 42", n.getMessage());
            assertEquals(0L, n.getSequenceNumber());
            assertNull(n.getUserData());
        } finally {
            releaseManagementConnector();
        }
    }

    @Test
    public void testForOpaqueProperty() throws JMException {
        testAttribute("opaqueAttr",
                TypeToken.of(byte[].class),
                new byte[]{10, 20, 30, 40, 50},
                arrayEquator(),
                false);
    }

    @Test
    public void testForIpAddressProperty() throws JMException {
        testAttribute("ipAddressAsByte",
                TypeToken.of(byte[].class),
                new IpAddress("192.168.0.1").toByteArray(),
                arrayEquator(),
                false);
        testAttribute("ipAddressAsString",
                TypeToken.of(String.class),
                "192.168.0.1",
                AbstractResourceConnectorTest.<String>valueEquator(),
                false);
    }

    @Test
    public void testForOidProperty() throws JMException {
        testAttribute("oidAsIntArray",
                TypeToken.of(int[].class),
                new OID("1.4.5.3.1").getValue(),
                arrayEquator(),
                false);
        testAttribute("oidAsString",
                TypeToken.of(String.class),
                "1.4.5.3.1",
                AbstractResourceConnectorTest.<String>valueEquator(),
                false);
    }

    @Test
    public void testForGauge32Property() throws JMException {
        testAttribute("gauge",
                TypeToken.of(Long.class),
                42L,
                false);
    }

    @Test
    public void testForCounter64Property() throws JMException {
        testAttribute("counter64",
                TypeToken.of(Long.class),
                42L,
                false);
    }

    @Test
    public void testForCounter32Property() throws JMException {
        testAttribute("counter32",
                TypeToken.of(Long.class),
                42L,
                AbstractResourceConnectorTest.<Long>valueEquator(),
                false);
    }

    @Test
    public void testForTimeTicksProperty() throws JMException {
        testAttribute("timeTicksAsLong",
                TypeToken.of(Long.class),
                642584970L,
                false);
        testAttribute("timeTicksAsString",
                TypeToken.of(String.class),
                new TimeTicks(642584974L).toString(),
                false);
    }

    @Test
    public void testForUnsignedInteger32Property() throws JMException {
        testAttribute("uint32",
                TypeToken.of(Long.class),
                42L,
                false);
    }

    @Test
    public void testForInteger32Property() throws JMException {
        testAttribute("int32",
                TypeToken.of(Integer.class),
                42,
                false);
    }

    @Test
    public void testForOctetStringProperty() throws IOException, JMException {
        testAttribute("octetstring",
                TypeToken.of(String.class),
                "Jack Ryan",
                false);
        testAttribute("hexstring",
                TypeToken.of(String.class),
                new OctetString("Java Enterprise Edition").toHexString(),
                false);
        testAttribute("octetstringAsByteArray",
                TypeToken.of(byte[].class),
                new byte[]{10, 20, 1, 4},
                arrayEquator(),
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
    public void configurationDescriptionServiceTest() {
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "snmpConversionFormat",
                "responseTimeout"
        ));
        testConfigurationDescriptor(EventConfiguration.class, ImmutableSet.of(
                "messageTemplate",
                "severity"
        ));
        testConfigurationDescriptor(AgentConfiguration.ManagedResourceConfiguration.class, ImmutableSet.of(
                "community",
                "engineID",
                "userName",
                "authenticationProtocol",
                "encryptionKey",
                "encryptionProtocol",
                "password",
                "localAddress",
                "securityContext",
                "queueSize",
                "priority",
                "keepAliveTime",
                "minPoolSize",
                "maxPoolSize",
                "smartMode"
        ));
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        agent.stop();
    }
}
