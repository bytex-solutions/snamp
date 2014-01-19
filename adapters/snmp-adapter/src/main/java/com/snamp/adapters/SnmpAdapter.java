package com.snamp.adapters;

import java.io.*;
import java.lang.Thread;
import java.net.BindException;
import java.util.*;
import java.util.logging.*;

import com.snamp.connectors.*;
import com.snamp.connectors.util.AbstractNotificationListener;
import com.snamp.licensing.*;
import net.xeoh.plugins.base.annotations.*;
import net.xeoh.plugins.base.annotations.meta.Author;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;
import static com.snamp.connectors.NotificationSupport.Notification;
import static com.snamp.adapters.SnmpHelpers.DateTimeFormatter;

/**
 * Represents SNMP Agent.
 * 
 * @author agrishin
 * 
 */
@PluginImplementation
@Author(name = "Roman Sakno")
final class SnmpAdapter extends SnmpAdapterBase implements LicensedPlatformPlugin<SnmpAdapterLimitations> {
    private static final String PASSWORD_PARAM = "password";
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";
    private static final OctetString V2C_TAG = new OctetString("v2c");
    private static final OctetString V3NOTIFY_TAG = new OctetString("v3notify");

    /**
     * Returns license limitations associated with this plugin.
     *
     * @return The license limitations applied to this plugin.
     */
    @Override
    public final SnmpAdapterLimitations getLimitations() {
        return SnmpAdapterLimitations.current();
    }

    /**
     * Represents a collection of MO's with OID postfixes.
     */
    private static final class ManagementAttributes extends HashMap<String, SnmpAttributeMapping>{
        public ManagementAttributes(){
            super(5);
        }
    }

    private final static class NotificationReceiverInfo {
        public static final String EVENT_TARGET_NAME = "receiverName";
        public static final String EVENT_TARGET_ADDRESS = "receiverAddress";
        private final OctetString name;
        private final String address;

        public NotificationReceiverInfo(final String name, final String address){
            this.name = new OctetString(name);
            this.address = address;
        }

        public NotificationReceiverInfo(final Map<String, String> options){
            this(options.get(EVENT_TARGET_NAME), options.get(EVENT_TARGET_ADDRESS));
        }

        public final OctetString getName(){
            return name;
        }

        public final OctetString getAddress(){
            final TransportIpAddress addr = new UdpAddress(address);
            return new OctetString(addr.getValue());
        }

        private static OID kindOfIP(final String addr){
            if (addr.indexOf(":") >= 0)
                return TransportDomains.transportDomainUdpIpv6;
            else if (addr.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+"))
                return TransportDomains.transportDomainUdpIpv4;
            return TransportDomains.transportDomainUdpDns;
        }

        public final OID getTransportDomain(){
            return kindOfIP(address);
        }
    }

    /**
     * Used for converting Management Connector notification into SNMP traps and sending traps.
     * This class cannot be inherited.
     */
    private final static class TrapSender extends AbstractNotificationListener{
        public static final int DEFAULT_TIMEOUT = 15000;//default timeout is 15 sec
        public static final int DEFAULT_RETRIES = 3;
        public static final String EVENT_TIMESTAMP_FORMAT = "timestampFormat";
        public static final String EVENT_TARGET_NOTIF_TIMEOUT = "sendingTimeout";
        public static final String EVENT_TARGET_RETRY_COUNT = "retryCount";
        private final NotificationReceiverInfo receiver;
        private final int timeout;
        private final int retries;
        private final OID eventId;
        private NotificationOriginator originator;
        private final DateTimeFormatter timestampFormatter;

        public TrapSender(final OID eventId,
                          NotificationReceiverInfo receiver,
                          final int timeout,
                          final int retries,
                          final DateTimeFormatter timestampFormatter){
            super(eventId.toString());
            this.eventId = eventId;
            this.receiver = receiver;
            this.timeout = timeout;
            this.retries = retries;
            this.timestampFormatter = timestampFormatter;
        }

        public final boolean registerTrap(final SnmpTargetMIB targetMIB, final OctetString paramsGroup, final NotificationOriginator originator){
            if(this.originator != null || originator == null) return false;
            else this.originator = originator;
            return targetMIB.addTargetAddress(receiver.getName(),
                    receiver.getTransportDomain(),
                    receiver.getAddress(),
                    timeout,
                    retries,
                    new OctetString("notify"),
                    paramsGroup,
                    StorageType.nonVolatile);
        }

        public final boolean unregisterTrap(final SnmpTargetMIB targetMIB){
            originator = null;
            return targetMIB.removeTargetAddress(receiver.getAddress()) != null;
        }

        private final boolean handle(final SnmpWrappedNotification n){
            return originator != null && n.send(new OctetString("public"), originator);
        }

        @Override
        public final boolean handle(final Notification n) {
            return handle(new SnmpWrappedNotification(eventId, n, timestampFormatter));
        }
    }

	private String address;
    private int port;
    private int socketTimeout;
    private boolean coldStart;
    private final Map<String, ManagementAttributes> attributes;
    private final Map<String, TrapSender> senders;
	
	public SnmpAdapter() throws IOException {
		// These files does not exist and are not used but has to be specified
		// Read snmp4j docs for more info
		super(new File("conf.agent"), null,
				new CommandProcessor(
						new OctetString(MPv3.createLocalEngineID())));
        SnmpAdapterLimitations.current().verifyPluginVersion(getClass());
        coldStart = true;
        port = defaultPort;
        address = defaultAddress;
        attributes = new HashMap<>();
        this.senders = new HashMap<>(10);
        this.socketTimeout = 0;
	}

    private static void registerManagedObjects(final MOServer server, final VacmMIB mib, final String prefix, Iterable<SnmpAttributeMapping> mos){

        boolean wasReadViewAdded = false;
        boolean wasWriteViewAdded = false;

        for(final SnmpAttributeMapping mo: mos)
            try {

                if (mo.getMetadata().canRead() && !wasReadViewAdded)
                {
                    mib.addViewTreeFamily(new OctetString("fullReadView"), new OID(prefix),
                            new OctetString(), VacmMIB.vacmViewIncluded,
                            StorageType.nonVolatile);
                    wasReadViewAdded = true;
                }
                if (mo.getMetadata().canWrite() && !wasWriteViewAdded)
                {
                    mib.addViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix),
                            new OctetString(), VacmMIB.vacmViewIncluded,
                            StorageType.nonVolatile);
                    wasWriteViewAdded = true;
                }

                server.register(mo, null);
            } catch (final DuplicateRegistrationException e) {
                log.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
    }

	@Override
	protected void registerManagedObjects() {
        for(final String prefix: attributes.keySet())
            registerManagedObjects(this.getServer(), this.getVacmMIB(), prefix, attributes.get(prefix).values());
	}

    private static String combineOID(final String prefix, final String postfix){
        return String.format("%s.%s", prefix, postfix);
    }

    /**
     * Unregisters additional managed objects from the agent's server.
     */
    @Override
    protected final void unregisterManagedObjects() {
        for(final String prefix: attributes.keySet()){
            for(final SnmpAttributeMapping mo: attributes.get(prefix).values()) if(mo != null) server.unregister(mo, null);
            this.getVacmMIB().removeViewTreeFamily(new OctetString("fullReadView"), new OID(prefix));
            this.getVacmMIB().removeViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix));
        }
    }

    /**
	 * Setup minimal View-based Access Control.
     * @param vacm View-based Access Control.
     * @see <a href='http://www.faqs.org/rfcs/rfc2575.html'>RFC-2575</a>
	 */
	@Override
	protected final void addViews(final VacmMIB vacm) {
            vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(
                    "cpublic"), new OctetString("v1v2group"),
                    StorageType.nonVolatile);

            vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                    SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV,
                    MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView"),
                    new OctetString("fullWriteView"), new OctetString(
                            "fullNotifyView"), StorageType.nonVolatile);
	}

	/**
	 * Initializes SNMPv3 users.
     * @param usm User-based security model.
	 */
	protected final void addUsmUser(final USM usm) {
    }

    /**
     * Adds initial notification targets and filters.
     *
     * @param targetMIB       the SnmpTargetMIB holding the target configuration.
     * @param notificationMIB the SnmpNotificationMIB holding the notification (filter)
     *                        configuration.
     */
    @Override
    protected final void addNotificationTargets(final SnmpTargetMIB targetMIB, final SnmpNotificationMIB notificationMIB) {
        if(senders.size() > 0){
            //add default address parsers for transport domains
            targetMIB.addDefaultTDomains();
            //register senders
            for(final String prefix: senders.keySet()){
                final TrapSender sender = senders.get(prefix);
                sender.registerTrap(targetMIB, V2C_TAG, getNotificationOriginator());
                sender.registerTrap(targetMIB, V3NOTIFY_TAG, getNotificationOriginator());
                getVacmMIB().addViewTreeFamily(new OctetString("fullNotifyView"), new OID(prefix),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
            }
            //setup internal SNMP settings
            targetMIB.addTargetParams(V2C_TAG,
                    MessageProcessingModel.MPv2c,
                    SecurityModel.SECURITY_MODEL_SNMPv2c,
                    new OctetString("cpublic"),
                    SecurityLevel.AUTH_PRIV,
                    StorageType.permanent);
            targetMIB.addTargetParams(V3NOTIFY_TAG,
                    MessageProcessingModel.MPv3,
                    SecurityModel.SECURITY_MODEL_USM,
                    new OctetString("v3notify"),
                    SecurityLevel.NOAUTH_NOPRIV,
                    StorageType.permanent);
            notificationMIB.addNotifyEntry(new OctetString("default"),
                    new OctetString("notify"),
                    SnmpNotificationMIB.SnmpNotifyTypeEnum.inform,
                    StorageType.permanent);
        }
    }

    private void removeNotificationTargets(){
        //setup internal SNMP settings
        getSnmpTargetMIB().removeTargetParams(V2C_TAG);
        getSnmpTargetMIB().removeTargetParams(V3NOTIFY_TAG);
        for(final String prefix: senders.keySet()){
            senders.get(prefix).unregisterTrap(getSnmpTargetMIB());
            this.getVacmMIB().removeViewTreeFamily(new OctetString("fullNotifyView"), new OID(prefix));
        }
    }

    /**
	 * Initializes SNMP transport.
	 */
	protected void initTransportMappings() throws IOException {
        final TransportMappings mappings = TransportMappings.getInstance();
        try{
            TransportMapping<?> tm = mappings.createTransportMapping(GenericAddress.parse(String.format("%s/%s", address, port)));
            if(tm instanceof DefaultUdpTransportMapping)
                ((DefaultUdpTransportMapping)tm).setSocketTimeout(socketTimeout);
            transportMappings = new TransportMapping[]{tm};
        }
        catch (final RuntimeException e){
            throw new IOException(String.format("Unable to create SNMP transport for %s/%s address.", address, port), e);
        }
	}

	private void start() throws IOException {
		switch (agentState){
            case STATE_STOPPED:
            case STATE_CREATED:
                init();
                if(coldStart) getServer().addContext(new OctetString("public"));
                finishInit();
                run();
                if(coldStart) sendColdStartNotification();
                coldStart = false;
            return;
            default: throw new IOException(String.format("SNMP agent already started (state %s).", agentState));
        }
	}

	/**
	 * Initializes SNMP communities.
	 */
	protected void addCommunities(final SnmpCommunityMIB communityMIB) {
		final Variable[] com2sec = new Variable[] { new OctetString("public"), // community
																			// name
				new OctetString("cpublic"), // security name
				getAgent().getContextEngineID(), // local engine ID
				new OctetString("public"), // default context name
				new OctetString(), // transport tag
				new Integer32(StorageType.nonVolatile), // storage type
				new Integer32(RowStatus.active) // row status
		};
		final MOTableRow row = communityMIB.getSnmpCommunityEntry().createRow(
				new OctetString("public2public").toSubIndex(true), com2sec);
		communityMIB.getSnmpCommunityEntry().addRow(row);
	}

    private boolean start(final Integer port, final String address, final int socketTimeout) throws IOException{
        this.port = port != null ? port.intValue() : defaultPort;
        this.address = address != null && address.length() > 0 ? address : defaultAddress;
        this.socketTimeout = socketTimeout;
        start();
        return true;
    }

    /**
     * Exposes the connector to the world.
     *
     * @param parameters The adapter startup parameters.
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    public boolean start(final Map<String, String> parameters) throws IOException{
        switch (agentState){
            case STATE_CREATED:
            case STATE_STOPPED:
                if(parameters.containsKey(PASSWORD_PARAM))
                    SnmpAdapterLimitations.current().verifyAuthenticationFeature();
                final String port = parameters.containsKey(PORT_PARAM_NAME) ? parameters.get(PORT_PARAM_NAME) : "161";
                final String address = parameters.containsKey(ADDRESS_PARAM_NAME) ? parameters.get(ADDRESS_PARAM_NAME) : "127.0.0.1";
                final String socketTimeout = parameters.containsKey(SOCKET_TIMEOUT_PARAM) ? parameters.get(SOCKET_TIMEOUT_PARAM) : "0";
                return start(Integer.valueOf(port), address, Integer.valueOf(socketTimeout));
            default:return false;
        }
    }

    /**
     * Stops the connector hosting.
     *
     * @param saveState {@literal true} to save previously exposed attributes for reuse; otherwise,
     *                       clear internal list of exposed attributes.
     * @return {@literal true}, if adapter is previously started; otherwise, {@literal false}.
     */
    @Override
    public boolean stop(final boolean saveState) {
        switch (agentState){
            case STATE_RUNNING:
                super.stop();
                removeNotificationTargets();
                unregisterSnmpMIBs();
                if(!saveState) {
                    attributes.clear();
                    senders.clear();
                }
                return true;
            default:return false;
        }
    }

    private void exposeAttribute(final AttributeSupport connector, final String prefix, final String postfix, AttributeConfiguration attribute){
        final String oid = combineOID(prefix, postfix);
        final SnmpAttributeMapping mo = SnmpType.createManagedObject(connector, oid, attribute.getAttributeName(), attribute.getAdditionalElements(), attribute.getReadWriteTimeout());
        if(mo == null)
            log.warning(String.format("Unable to expose %s attribute with OID %s", attribute.getAttributeName(), oid));
        else {
            final ManagementAttributes attributes;
            if(this.attributes.containsKey(prefix)) attributes = this.attributes.get(prefix);
            else this.attributes.put(prefix, attributes = new ManagementAttributes());
            if(attributes.containsKey(postfix))
                log.warning(String.format("Duplicated attribute %s detected.", oid));
            else attributes.put(postfix, mo);
        }
    }

    /**
     * Exposes management attributes.
     *
     * @param connector The attribute value provider.
     * @param namespace  The attributes namespace.
     * @param attributes The dictionary of attributes.
     */
    @Override
    public final void exposeAttributes(final AttributeSupport connector, final String namespace, final Map<String, AttributeConfiguration> attributes) {
        for(final String postfix: attributes.keySet())
            exposeAttribute(connector, namespace, postfix, attributes.get(postfix));
    }

    private final void exposeEvent(final NotificationSupport connector, final String namespace, final String postfix, final EventConfiguration eventInfo){
        final Map<String, String> eventOptions = eventInfo.getAdditionalElements();
        if(eventOptions.containsKey(NotificationReceiverInfo.EVENT_TARGET_ADDRESS) && eventOptions.containsKey(NotificationReceiverInfo.EVENT_TARGET_NAME)){
            final int timeout = eventOptions.containsKey(TrapSender.EVENT_TARGET_NOTIF_TIMEOUT) ?
                    Integer.valueOf(eventOptions.get(TrapSender.EVENT_TARGET_NOTIF_TIMEOUT)):
                    TrapSender.DEFAULT_TIMEOUT;
            final int retryCount = eventOptions.containsKey(TrapSender.EVENT_TARGET_RETRY_COUNT) ?
                    Integer.valueOf(eventOptions.get(TrapSender.EVENT_TARGET_RETRY_COUNT)) : TrapSender.DEFAULT_RETRIES;
            final DateTimeFormatter timestampFormatter = SnmpHelpers.createDateTimeFormatter(eventOptions.get(TrapSender.EVENT_TIMESTAMP_FORMAT));
            final TrapSender sender = new TrapSender(new OID(combineOID(namespace, postfix)),
                    new NotificationReceiverInfo(eventOptions),
                    timeout,
                    retryCount,
                    timestampFormatter);
            senders.put(namespace, sender);
            //now we should register listener inside of management connector
            connector.enableNotifications(sender.getSubscriptionListId(), eventInfo.getCategory(), eventInfo.getAdditionalElements());
            sender.attachTo(connector);
        }
    }

    @Override
    public final void exposeEvents(final NotificationSupport connector, final String namespace, final Map<String, EventConfiguration> events) {
        for(final String postfix: events.keySet())
            exposeEvent(connector, namespace, postfix, events.get(postfix));
    }

    /**
     * Releases all resources associated with this adapter.
     */
    @Override
    public void close() {
        switch (agentState){
            case STATE_RUNNING: super.stop();
            case STATE_STOPPED:
            case STATE_CREATED:
                removeNotificationTargets();
                unregisterSnmpMIBs();
            break;
            default:
                log.log(Level.SEVERE, String.format("Unknown SNMP agent state: %s", agentState)); break;
        }
        attributes.clear();
        senders.clear();
    }
}