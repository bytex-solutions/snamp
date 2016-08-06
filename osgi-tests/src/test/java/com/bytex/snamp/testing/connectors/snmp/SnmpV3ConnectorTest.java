package com.bytex.snamp.testing.connectors.snmp;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
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
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;

import javax.management.JMException;
import javax.management.Notification;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.bytex.snamp.configuration.EntityMap;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Represents SNMPv3 connector test.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnmpV3ConnectorTest extends AbstractSnmpConnectorTest {
    private static final String HOST_NAME = "127.0.0.1";
    private static final int REMOTE_PORT = 1161;
    private static final int LOCAL_PORT = 44495;
    private static final String ENGINE_ID = "80:00:13:70:01:7f:00:01:01:be:1e:8b:35";
    private static final String USER_NAME = "roman";
    private static final String PASSWORD = "somePassword";
    private static final String AUTH_PROTOCOL = "sha";
    private static final String ENC_PROTOCOL = "aes128";
    private static final String ENC_KEY = "samplekey";

    private static Map<String, String> getParameters(final int localPort){
        final Map<String, String> params = new HashMap<>(7);
        params.put("localAddress", "udp://127.0.0.1/" + localPort);
        params.put("engineID", ENGINE_ID);
        params.put("userName", USER_NAME);
        params.put("password", PASSWORD);
        params.put("authenticationProtocol", AUTH_PROTOCOL);
        params.put("encryptionProtocol", ENC_PROTOCOL);
        params.put("encryptionKey", ENC_KEY);
        return params;
    }

    private static Map<String, String> getParameters() {
        return getParameters(LOCAL_PORT);
    }

    private final BaseAgent agent;

    public SnmpV3ConnectorTest(){
        super(HOST_NAME, REMOTE_PORT, getParameters());
        agent = new BaseAgent(new File("conf.agent"), null,
                new CommandProcessor(
                        OctetString.fromHexString(ENGINE_ID))) {
            private static final String GROUP_NAME = "testGroup";
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
                }
                catch (final DuplicateRegistrationException e) {
                    fail(e.getMessage());
                }
            }

            protected void initTransportMappings() throws IOException {
                final TransportMappings mappings = TransportMappings.getInstance();
                try{
                    TransportMapping<?> tm = mappings.createTransportMapping(GenericAddress.parse(String.format("%s/%s", HOST_NAME, REMOTE_PORT)));
                    if(tm instanceof DefaultUdpTransportMapping)
                        ((DefaultUdpTransportMapping)tm).setSocketTimeout(5000);
                    transportMappings = new TransportMapping<?>[]{tm};
                }
                catch (final RuntimeException e){
                    throw new IOException(String.format("Unable to create SNMP transport for %s/%s address.", HOST_NAME, REMOTE_PORT), e);
                }
            }

            @Override
            protected void unregisterManagedObjects() {
            }

            @Override
            protected void addUsmUser(final USM usm) {
                usm.addUser(new OctetString(USER_NAME),
                        OctetString.fromHexString(ENGINE_ID),
                        new UsmUser(new OctetString(USER_NAME), AuthSHA.ID, new OctetString(PASSWORD), PrivAES128.ID, new OctetString(ENC_KEY)));
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
                        MessageProcessingModel.MPv3,
                        SecurityModel.SECURITY_MODEL_USM,
                        new OctetString(USER_NAME),
                        SecurityLevel.AUTH_PRIV,
                        StorageType.permanent);
                notificationMIB.addNotifyEntry(new OctetString("default"),
                        new OctetString("notify"),
                        SnmpNotificationMIB.SnmpNotifyTypeEnum.trap,
                        StorageType.permanent);
            }

            @Override
            protected void addViews(final VacmMIB vacm) {
                vacm.addGroup(SecurityModel.SECURITY_MODEL_USM,
                        new OctetString(USER_NAME),
                        new OctetString(GROUP_NAME),
                        StorageType.nonVolatile);
                vacm.addAccess(new OctetString(GROUP_NAME), new OctetString(),
                        SecurityModel.SECURITY_MODEL_USM, SecurityLevel.AUTH_PRIV,
                        MutableVACM.VACM_MATCH_EXACT,
                        new OctetString("fullReadView"),
                        new OctetString("fullWriteView"),
                        new OctetString("fullNotifyView"),
                        StorageType.nonVolatile);
                vacmMIB.addViewTreeFamily(new OctetString("fullReadView"), new OID("1.6"),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
                vacmMIB.addViewTreeFamily(new OctetString("fullWriteView"), new OID("1.6"),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
                vacmMIB.addViewTreeFamily(new OctetString("fullNotifyView"), new OID("1.7"),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void addCommunities(final SnmpCommunityMIB communityMIB) {
                final Variable[] com2sec = new Variable[] { new OctetString("public"), // community
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
                if(notifSender != null)
                    try {
                        notifSender.stop(Duration.ofSeconds(1));
                    }
                    catch (final Exception e) {
                        fail(e.getMessage());
                    }
                notifSender = null;
                super.stop();

            }

            @Override
            public void init() throws IOException {
                super.init();
                if(coldStart) getServer().addContext(new OctetString("public"));
                finishInit();
                run();
                if(coldStart) sendColdStartNotification();
                coldStart = false;
                notifSender = new Repeater(Duration.ofSeconds(1)) {
                    @Override
                    protected void doAction() {
                        final VariableBinding[] bindings = {
                                new VariableBinding(new OID("1.7.1.1.0"), new OctetString("Hello, world!")),
                                new VariableBinding(new OID("1.7.1.2.0"), new Integer32(42))
                        };
                        if(notificationOriginator != null)
                            notificationOriginator.notify(new OctetString(), new OID("1.7.1"), bindings);
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
            final CompletableFuture<Notification> trap = new CompletableFuture<>();
            notifications.addNotificationListener((notification, handback) -> trap.complete(notification), null, null);
            //obtain client addresses
            final Address[] addresses = connector.queryObject(Address[].class);
            assertNotNull(addresses);
            assertEquals(1, addresses.length);
            assertTrue(addresses[0] instanceof UdpAddress);
            final Notification n = trap.get(6, TimeUnit.SECONDS);
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
                ArrayUtils::strictEquals,
                false);
    }

    @Test
    public void testForIpAddressProperty() throws JMException {
        testAttribute("ipAddressAsByte",
                TypeToken.of(byte[].class),
                new IpAddress("192.168.0.1").toByteArray(),
                ArrayUtils::strictEquals,
                false);
        testAttribute("ipAddressAsString",
                TypeToken.of(String.class),
                "192.168.0.1",
                Objects::equals,
                false);
    }

    @Test
    public void testForOidProperty() throws JMException {
        testAttribute("oidAsIntArray",
                TypeToken.of(int[].class),
                new OID("1.4.5.3.1").getValue(),
                ArrayUtils::strictEquals,
                false);
        testAttribute("oidAsString",
                TypeToken.of(String.class),
                "1.4.5.3.1",
                Objects::equals,
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
                Objects::equals,
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
                ArrayUtils::strictEquals,
                false);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        agent.init();
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) throws Exception {
        stopResourceConnector(context);
        agent.stop();
    }
}