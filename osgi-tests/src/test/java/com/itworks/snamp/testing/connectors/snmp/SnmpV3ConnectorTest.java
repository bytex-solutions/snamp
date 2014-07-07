package com.itworks.snamp.testing.connectors.snmp;

import com.itworks.snamp.Repeater;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.testing.connectors.AbstractManagementConnectorTest;
import org.apache.commons.lang3.ArrayUtils;
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
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Represents SNMPv3 connector test.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpV3ConnectorTest extends AbstractSnmpConnectorTest {
    private static final String HOST_NAME = "127.0.0.1";
    private static final int REMOTE_PORT = 1161;
    private static final int LOCAL_PORT = 44495;
    private static final String ENGINE_ID = "Non-local engine ID";
    private static final String USER_NAME = "roman";
    private static final String PASSWORD = "somePassword";
    private static final String AUTH_PROTOCOL = "sha";
    private static final String ENC_PROTOCOL = "aes128";
    private static final String ENC_KEY = "samplekey";

    private static Map<String, String> getParameters(){
        final Map<String, String> params = new HashMap<>(1);
        params.put("localAddress", "udp://127.0.0.1/" + LOCAL_PORT);
        params.put("engineID", ENGINE_ID);
        params.put("userName", USER_NAME);
        params.put("password", PASSWORD);
        params.put("authenticationProtocol", AUTH_PROTOCOL);
        params.put("encryptionProtocol", ENC_PROTOCOL);
        params.put("encryptionKey", ENC_KEY);
        return params;
    }

    private final BaseAgent agent;

    public SnmpV3ConnectorTest(){
        super(HOST_NAME, REMOTE_PORT, getParameters());
        agent = new BaseAgent(new File("conf.agent"), null,
                new CommandProcessor(
                        new OctetString(ENGINE_ID))) {
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
                    transportMappings = new TransportMapping[]{tm};
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
                        new OctetString(ENGINE_ID),
                        new UsmUser(new OctetString(USER_NAME), AuthSHA.ID, new OctetString(PASSWORD), PrivAES128.ID, new OctetString(ENC_KEY)));
            }

            @Override
            protected void addNotificationTargets(final SnmpTargetMIB targetMIB, final SnmpNotificationMIB notificationMIB) {

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
                if(notifSender != null)
                    try {
                        notifSender.stop(TimeSpan.fromSeconds(1));
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
                notifSender = new Repeater(TimeSpan.fromSeconds(1)) {
                    @Override
                    protected void doAction() {
                        final VariableBinding[] bindings = {
                                new VariableBinding(new OID("1.7.1.1.0"), new OctetString("Hello, world!")),
                                new VariableBinding(new OID("1.7.1.2.0"), new Integer32(42))
                        };
                        if(notificationOriginator != null)
                            notificationOriginator.notify(new OctetString("public"), new OID("1.7.1"), bindings);
                    }
                };
                notifSender.run();
            }
        };
    }

    @Test
    public void testForOpaqueProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.10.0";
        testAttribute(ATTRIBUTE_ID,
                Object[].class,
                new Byte[]{10, 20, 30, 40, 50},
                AbstractManagementConnectorTest.arrayEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForIpAddressProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.9.0";
        testAttribute(ATTRIBUTE_ID,
                Object[].class,
                ArrayUtils.toObject(new IpAddress("192.168.0.1").toByteArray()),
                AbstractManagementConnectorTest.arrayEquator(),
                Collections.<String, String>emptyMap(),
                false);
        testAttribute(ATTRIBUTE_ID,
                String.class,
                "192.168.0.1",
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }

    @Test
    public void testForOidProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.8.0";
        testAttribute(ATTRIBUTE_ID,
                Object[].class,
                ArrayUtils.toObject(new OID("1.4.5.3.1").getValue()),
                AbstractManagementConnectorTest.arrayEquator(),
                Collections.<String, String>emptyMap(),
                false);
        testAttribute(ATTRIBUTE_ID,
                String.class,
                "1.4.5.3.1",
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }

    @Test
    public void testForGauge32Property() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.7.0";
        testAttribute(ATTRIBUTE_ID,
                Long.class,
                42L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForCounter64Property() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.6.0";
        testAttribute(ATTRIBUTE_ID,
                Long.class,
                42L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForCounter32Property() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.5.0";
        testAttribute(ATTRIBUTE_ID,
                Long.class,
                42L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForTimeTicksProperty() throws TimeoutException, IOException{
        final String ATTRIBUTE_ID = "1.6.4.0";
        testAttribute(ATTRIBUTE_ID,
                Long.class,
                642584970L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
        testAttribute(ATTRIBUTE_ID,
                String.class,
                new TimeTicks(642584974L).toString(),
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "text");
                }},
                false);
    }

    @Test
    public void testForUnsignedInteger32Property() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.3.0";
        testAttribute(ATTRIBUTE_ID,
                Long.class,
                42L,
                AbstractManagementConnectorTest.<Long>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForInteger32Property() throws TimeoutException, IOException{
        final String ATTRIBUTE_ID = "1.6.2.0";
        testAttribute(ATTRIBUTE_ID,
                Integer.class,
                42,
                AbstractManagementConnectorTest.<Integer>valueEquator(),
                Collections.<String, String>emptyMap(),
                false);
    }

    @Test
    public void testForOctetStringProperty() throws TimeoutException, IOException {
        final String ATTRIBUTE_ID = "1.6.1.0";
        testAttribute(ATTRIBUTE_ID,
                String.class,
                "Jack Ryan",
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "text");
                }},
                false);
        testAttribute(ATTRIBUTE_ID,
                String.class,
                new OctetString("Java Enterprise Edition").toHexString(),
                AbstractManagementConnectorTest.<String>valueEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "hex");
                }},
                false);
        testAttribute(ATTRIBUTE_ID,
                Object[].class,
                new Byte[]{10, 20, 1, 4},
                AbstractManagementConnectorTest.arrayEquator(),
                new HashMap<String, String>(1){{
                    put("snmpConversionFormat", "raw");
                }},
                false);
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        agent.init();
    }

    @Override
    protected void afterCleanupTest(final BundleContext context) {
        agent.stop();
    }
}
