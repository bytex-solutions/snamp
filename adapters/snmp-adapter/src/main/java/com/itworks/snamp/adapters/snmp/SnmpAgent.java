package com.itworks.snamp.adapters.snmp;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.NotificationSupport;
import com.itworks.snamp.connectors.util.*;
import com.itworks.snamp.licensing.LicensedPlatformPlugin;

import net.xeoh.plugins.base.annotations.*;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.*;
import org.snmp4j.mp.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;
import static com.itworks.snamp.connectors.NotificationSupport.Notification;
import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;

/**
 * Represents SNMP Agent.
 * 
 * @author Roman Sakno, Evgeniy Kirichenko
 * 
 */
final class SnmpAgent extends BaseAgent {
    private static final OctetString NOTIFICATION_SETTINGS_TAG = new OctetString("NOTIF_TAG");
    private static final Logger logger = SnmpHelpers.getLogger();

    /**
     * Used for converting Management Connector notification into SNMP traps and sending traps.
     * This class cannot be inherited.
     */
    private final static class TrapSender extends AbstractNotificationListener {
        public static final int DEFAULT_TIMEOUT = 15000;//default timeout is 15 sec
        public static final int DEFAULT_RETRIES = 3;
        private int timeout;
        private int retries;
        private NotificationOriginator originator;
        private SnmpHelpers.DateTimeFormatter timestampFormatter;
        private final OctetString receiverName;
        private final String receiverAddress;

        public TrapSender(final String eventId,
                          final String receiverName,
                          final String receiverAddress){
            super(eventId);
            timestampFormatter = SnmpHelpers.createDateTimeFormatter(null);
            this.timeout = 0;
            this.retries = 3;
            this.receiverName = new OctetString(receiverName);
            this.receiverAddress = receiverAddress;
        }

        private static OID kindOfIP(final String addr){
            if (addr.indexOf(":") >= 0)
                return TransportDomains.transportDomainUdpIpv6;
            else if (addr.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+/\\d+"))
                return TransportDomains.transportDomainUdpIpv4;
            return TransportDomains.transportDomainUdpDns;
        }

        public final OID getTransportDomain(){
            return kindOfIP(receiverAddress);
        }

        public final void setTimeout(final int value){
            this.timeout = value;
        }

        public final void setRetryCount(final int value){
            this.retries = value;
        }

        public final void setTimestampFormatter(final String formatterName){
            timestampFormatter = SnmpHelpers.createDateTimeFormatter(formatterName);
        }

        public final OctetString getAddress(){
            final TransportIpAddress addr = new UdpAddress(receiverAddress);
            return new OctetString(addr.getValue());
        }

        public final boolean registerTrap(final SnmpTargetMIB targetMIB, final OctetString paramsGroup, final NotificationOriginator originator){
            if(this.originator != null || originator == null) return false;
            else this.originator = originator;
            return targetMIB.addTargetAddress(receiverName,
                    getTransportDomain(),
                    getAddress(),
                    timeout,
                    retries,
                    new OctetString("notify"),
                    paramsGroup,
                    StorageType.nonVolatile);
        }

        public final void unregisterTrap(){
            originator = null;
        }

        private final boolean handle(final SnmpWrappedNotification n){
            return originator != null &&
                    (n.send(new OctetString(), originator) //for SNMPv3 sending
                    || n.send(new OctetString("public"), originator)); //for SNMPv2 sending
        }

        @Override
        public final boolean handle(final Notification n, final String category) {
            return handle(new SnmpWrappedNotification(new OID(getSubscriptionListId()), n, category, timestampFormatter));
        }
    }

    private static final class TrapSendersMap extends EnabledNotifications<TrapSender> {

        public TrapSendersMap(final NotificationSupport connector){
            super(connector);
        }

        @Override
        public final String makeListId(final String prefix, final String postfix) {
            return new OID(prefix).append(postfix).toString();
        }

        public final void unregisterAll(final boolean detach){
            for(final TrapSender handler: values()){
                handler.unregisterTrap();
                if(detach) handler.detachFrom(connector);
            }
        }

        public final void registerAll(final String prefix, final SnmpTargetMIB targetMIB, final VacmMIB vacmMIB, final NotificationOriginator originator, final boolean attach){
            for(final TrapSender handler: values()){
                handler.registerTrap(targetMIB, NOTIFICATION_SETTINGS_TAG, originator);
                vacmMIB.addViewTreeFamily(new OctetString("fullNotifyView"), new OID(prefix),
                        new OctetString(), VacmMIB.vacmViewIncluded,
                        StorageType.nonVolatile);
                if(attach) handler.attachTo(connector);
            }
        }

        @Override
        public final TrapSender createDescription(final String prefix, final String postfix, final EventConfiguration config) {
            final Map<String, String> eventOptions = config.getAdditionalElements();
            if(eventOptions.containsKey(TARGET_ADDRESS_PARAM) && eventOptions.containsKey(TARGET_NAME_PARAM)){
                final int timeout = eventOptions.containsKey(TARGET_NOTIF_TIMEOUT_PARAM) ?
                        Integer.valueOf(eventOptions.get(TARGET_NOTIF_TIMEOUT_PARAM)):
                        TrapSender.DEFAULT_TIMEOUT;
                final int retryCount = eventOptions.containsKey(TARGET_RETRY_COUNT_PARAM) ?
                        Integer.valueOf(eventOptions.get(TARGET_RETRY_COUNT_PARAM)) : TrapSender.DEFAULT_RETRIES;
                final TrapSender sender = new TrapSender(makeListId(prefix, postfix),
                        eventOptions.get(TARGET_NAME_PARAM),
                        eventOptions.get(TARGET_ADDRESS_PARAM));
                sender.setRetryCount(retryCount);
                sender.setTimeout(timeout);
                sender.setTimestampFormatter(eventOptions.get(DATE_TIME_DISPLAY_FORMAT_PARAM));
                return sender;
            }
            else return null;   //null means that the sender will not be added to the registry
        }
    }

    /**
     * Represents a registry of enabled notifications. This class cannot be inherited.
     */
    private static final class ManagementNotifications extends AbstractSubscriptionList<TrapSender> {

        @Override
        protected final TrapSendersMap createBinding(final NotificationSupport connector) {
            return new TrapSendersMap(connector);
        }

        public final void cancelSubscription() {
            for(final String prefix: keySet()){
                final TrapSendersMap handlers = get(prefix, TrapSendersMap.class);
                handlers.unregisterAll(true);
            }
        }

        public final void forceSubscription(final SnmpTargetMIB targetMIB, final SnmpNotificationMIB notificationMIB, final VacmMIB vacmMIB, final SecurityConfiguration security, final NotificationOriginator originator){
            if(isEmpty()) return;
            targetMIB.addDefaultTDomains();
            //register senders
            for(final String prefix: keySet()){
                final TrapSendersMap handlers = get(prefix, TrapSendersMap.class);
                handlers.registerAll(prefix, targetMIB, vacmMIB, originator, true);
            }
            //setup internal SNMP settings
            if(security != null){
                //find the user with enabled notification
                final String notifyUser = security.findFirstUser(SecurityConfiguration.
                        createUserSelector(SecurityConfiguration.AccessRights.NOTIFY));
                //user for notification receiving exists
                if(notifyUser != null)
                    targetMIB.addTargetParams(NOTIFICATION_SETTINGS_TAG,
                            MessageProcessingModel.MPv3,
                            SecurityModel.SECURITY_MODEL_USM,
                            new OctetString(notifyUser),
                            security.getUserSecurityLevel(notifyUser).getSnmpValue(),
                            StorageType.permanent);
            }
            else targetMIB.addTargetParams(NOTIFICATION_SETTINGS_TAG,
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
    }

    private static final class ManagementAttributesMap extends ConnectedAttributes<SnmpAttributeMapping> {
        public ManagementAttributesMap(final AttributeSupport connector){
            super(connector);
        }

        private OID makeAttributeOID(final String prefix, final String postfix){
            return new OID(prefix).append(postfix);
        }

        @Override
        public final String makeAttributeId(final String prefix, final String postfix) {
            return makeAttributeOID(prefix, postfix).toString();
        }

        @Override
        public final SnmpAttributeMapping createDescription(final String prefix, final String postfix, final AttributeConfiguration config) {
            final SnmpAttributeMapping mo = SnmpType.createManagedObject(connector, makeAttributeId(prefix, postfix), config.getAttributeName(), config.getAdditionalElements(), config.getReadWriteTimeout());
            if(mo == null){
                getAdapterLogger().warning(String.format("Unable to expose %s attribute with OID %s", config.getAttributeName(), makeAttributeId(prefix, postfix)));
                return null;
            }
            else return mo;
        }

        public final void unregister(final MOServer server, final VacmMIB vacmMIB) {
            for(final SnmpAttributeMapping mo: values())
                server.unregister(mo, null);
        }
    }

    private static final class ManagementAttributes extends AbstractAttributesRegistry<SnmpAttributeMapping> {

        @Override
        protected final ManagementAttributesMap createBinding(final AttributeSupport connector) {
            return new ManagementAttributesMap(connector);
        }

        public final void unregister(final MOServer server, final VacmMIB vacmMIB) {
            for(final String prefix: keySet()){
                final ManagementAttributesMap attrs = get(prefix, ManagementAttributesMap.class);
                attrs.unregister(server, vacmMIB);
                vacmMIB.removeViewTreeFamily(new OctetString("fullReadView"), new OID(prefix));
                vacmMIB.removeViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix));
            }
        }

        public final void register(final MOServer server, final VacmMIB vacmMIB){
            for(final String prefix: keySet()){
                final ManagementAttributesMap attrs = get(prefix, ManagementAttributesMap.class);
                boolean wasReadViewAdded = false;
                boolean wasWriteViewAdded = false;
                for(final SnmpAttributeMapping mo: attrs.values())
                    try {
                        if (mo.getMetadata().canRead() && !wasReadViewAdded){
                            vacmMIB.addViewTreeFamily(new OctetString("fullReadView"), new OID(prefix),
                                    new OctetString(), VacmMIB.vacmViewIncluded,
                                    StorageType.nonVolatile);
                            wasReadViewAdded = true;
                        }
                        if (mo.getMetadata().canWrite() && !wasWriteViewAdded){
                            vacmMIB.addViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix),
                                    new OctetString(), VacmMIB.vacmViewIncluded,
                                    StorageType.nonVolatile);
                            wasWriteViewAdded = true;
                        }
                        server.register(mo, null);
                    }
                    catch (final DuplicateRegistrationException e) {
                        getAdapterLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
                    }
            }
        }
    }

	private final String hostName;
    private final int port;
    private final int socketTimeout;
    private boolean coldStart;
    private final ManagementAttributes attributes;
    private final ManagementNotifications senders;
    private SecurityConfiguration security;

	public SnmpAgent(final int port, final String hostName, final SecurityConfiguration securityOptions, final int socketTimeout) throws IOException {
		// These files does not exist and are not used but has to be specified
		// Read snmp4j docs for more info
		super(new File("conf.agent"), null,
                new CommandProcessor(
                        new OctetString(MPv3.createLocalEngineID())));
        coldStart = true;
        attributes = new ManagementAttributes();
        this.senders = new ManagementNotifications();
        this.hostName = hostName;
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.security = securityOptions;
	}

	@Override
	protected void registerManagedObjects() {
        attributes.register(server, getVacmMIB());
	}

    /**
     * Unregisters additional managed objects from the agent's server.
     */
    @Override
    protected final void unregisterManagedObjects() {
        attributes.unregister(server, getVacmMIB());
    }

    /**
	 * Setup minimal View-based Access Control.
     * @param vacm View-based Access Control.
     * @see <a href='http://www.faqs.org/rfcs/rfc2575.html'>RFC-2575</a>
	 */
	@Override
	protected final void addViews(final VacmMIB vacm) {
        if (security == null){
            vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, new OctetString(
                    "cpublic"), new OctetString("v1v2group"),
                    StorageType.nonVolatile);

            vacm.addAccess(new OctetString("v1v2group"), new OctetString("public"),
                    SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV,
                    MutableVACM.VACM_MATCH_EXACT, new OctetString("fullReadView"),
                    new OctetString("fullWriteView"), new OctetString(
                    "fullNotifyView"), StorageType.nonVolatile);
        }
        else security.setupViewBasedAcm(vacm);
	}

    /**
	 * Initializes SNMPv3 users.
     * @param usm User-based security model.
	 */
	protected final void addUsmUser(final USM usm) {
        if (security != null)
            security.setupUserBasedSecurity(usm);
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
        senders.forceSubscription(targetMIB, notificationMIB, vacmMIB,
                security,
                getNotificationOriginator());
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

	public boolean start() throws IOException {
		switch (agentState){
            case STATE_STOPPED:
            case STATE_CREATED:
                init();
                if(coldStart) getServer().addContext(new OctetString("public"));
                finishInit();
                run();
                if(coldStart) sendColdStartNotification();
                coldStart = false;
            return true;
            default:
                logger.log(Level.SEVERE, String.format("SNMP agent already started (state %s).", agentState));
            return false;
        }
	}

    /**
	 * Initializes SNMP communities.
	 */
	@SuppressWarnings("unchecked")
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
        if(agentState == STATE_RUNNING){
            super.stop();
            snmpTargetMIB.getSnmpTargetAddrEntry().removeAll();
            snmpTargetMIB.getSnmpTargetParamsEntry().removeAll();
            senders.cancelSubscription();
            unregisterSnmpMIBs();
            attributes.disconnect();
            attributes.clear();
            senders.disable();
            senders.clear();
        }
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
                snmpTargetMIB.getSnmpTargetAddrEntry().removeAll();
                snmpTargetMIB.getSnmpTargetParamsEntry().removeAll();
                senders.cancelSubscription();
                unregisterSnmpMIBs();
            break;
            default:
                logger.log(Level.SEVERE, String.format("Unknown SNMP agent state: %s", agentState)); break;
        }
        attributes.disconnect();
        attributes.clear();
        senders.disable();
        senders.clear();
    }
}