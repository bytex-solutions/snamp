package com.bytex.snamp.gateway.snmp;

import com.bytex.snamp.gateway.GatewayUpdatedCallback;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.DefaultSecurityProtocols;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.ConcurrentMessageDispatcher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import static com.bytex.snamp.gateway.snmp.OctetStringHelper.toOctetString;

/**
 * Represents SNMP Agent.
 * 
 * @author Roman Sakno, Evgeniy Kirichenko
 * 
 */
final class SnmpAgent extends BaseAgent implements SnmpNotificationListener, GatewayUpdatedCallback, Closeable {
    private static final OctetString NOTIFICATION_SETTINGS_TAG = toOctetString("NOTIF_TAG");

	private final String hostName;
    private final int port;
    private final int socketTimeout;
    private boolean coldStart;
    private final ExecutorService threadPool;
    private final SecurityConfiguration security;
    private final OID prefix;

    SnmpAgent(final OID prefix,
            final OctetString engineID,
            final int port,
            final String hostName,
            final SecurityConfiguration securityOptions,
            final int socketTimeout,
            final ExecutorService threadPool) throws IOException {
		// These files does not exist and are not used but has to be specified
		// Read snmp4j docs for more info
		super(new File("conf.agent"), null,
                new CommandProcessor(
                        new OctetString(engineID)));
        this.threadPool = Objects.requireNonNull(threadPool);
        coldStart = true;
        this.hostName = hostName;
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.security = securityOptions;
        this.prefix = prefix;
	}

    void registerManagedObject(final SnmpAttributeAccessor accessor) throws DuplicateRegistrationException {
        accessor.registerManagedObject(prefix, server);
    }

    ManagedObject unregisterManagedObject(final SnmpAttributeAccessor accessor) {
        return accessor.unregisterManagedObject(server);
    }

    void registerNotificationTarget(final SnmpNotificationMapping mapping){
        getSnmpTargetMIB().addTargetAddress(mapping.getReceiverName(),
                mapping.getTransportDomain(),
                mapping.getReceiverAddress(),
                mapping.getTimeout(),
                mapping.getRetryCount(),
                new OctetString("notify"),
                NOTIFICATION_SETTINGS_TAG,
                StorageType.nonVolatile);
        mapping.setNotificationOriginator(getNotificationOriginator());
    }

    private void unregisterNotificationTarget(final OctetString receiverName){
        getSnmpTargetMIB().removeTargetAddress(receiverName);
    }

    void unregisterNotificationTarget(final SnmpNotificationMapping mapping){
        unregisterNotificationTarget(mapping.getReceiverName());
        mapping.setNotificationOriginator(null);
    }

    private void addFullReadView(final OID... systemIDs){
        for(final OID prefix: systemIDs)
            vacmMIB.addViewTreeFamily(new OctetString("fullReadView"), prefix,
                    new OctetString(), VacmMIB.vacmViewIncluded,
                    StorageType.nonVolatile);
    }

	@Override
	protected void registerManagedObjects() {
        vacmMIB.addViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.volatile_);
        vacmMIB.addViewTreeFamily(new OctetString("fullReadView"), new OID(prefix),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.volatile_);
        addFullReadView(VacmMIB.vacmAccessEntryOID,
                VacmMIB.vacmContextEntryOID,
                VacmMIB.vacmViewTreeFamilyEntryOID,
                VacmMIB.vacmSecurityToGroupEntryOID,
                VacmMIB.vacmViewSpinLockOID);
    }

    private void removeFullReadView(final OID... systemIDs){
        for(final OID prefix: systemIDs)
            vacmMIB.removeViewTreeFamily(new OctetString("fullReadView"), prefix);
    }

    /**
     * Unregisters additional managed objects from the agent's server.
     */
    @Override
    protected void unregisterManagedObjects() {
        vacmMIB.removeViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix));
        vacmMIB.removeViewTreeFamily(new OctetString("fullReadView"), new OID(prefix));
        removeFullReadView(VacmMIB.vacmAccessEntryOID,
                VacmMIB.vacmContextEntryOID,
                VacmMIB.vacmViewTreeFamilyEntryOID,
                VacmMIB.vacmSecurityToGroupEntryOID,
                VacmMIB.vacmViewSpinLockOID);
    }

    /**
	 * Setup minimal View-based Access Control.
     * @param vacm View-based Access Control.
     * @see <a href='http://www.faqs.org/rfcs/rfc2575.html'>RFC-2575</a>
	 */
	@Override
	protected void addViews(final VacmMIB vacm) {
        if (security == null){
            vacm.addGroup(SecurityModel.SECURITY_MODEL_SNMPv2c, toOctetString(
                            "cpublic"),
                    toOctetString("v1v2group"),
                    StorageType.nonVolatile);

            vacm.addAccess(toOctetString("v1v2group"), toOctetString("public"),
                    SecurityModel.SECURITY_MODEL_ANY, SecurityLevel.NOAUTH_NOPRIV,
                    MutableVACM.VACM_MATCH_EXACT, toOctetString("fullReadView"),
                    toOctetString("fullWriteView"), toOctetString(
                            "fullNotifyView"), StorageType.nonVolatile);
        }
        else security.setupViewBasedAcm(vacm);
	}

    /**
	 * Initializes SNMPv3 users.
     * @param usm User-based security model.
	 */
	protected void addUsmUser(final USM usm) {
        if (security != null)
            security.setupUserBasedSecurity(usm);
    }

    /**
     * Initializes concurrent message dispatcher
     */
    protected void initMessageDispatcher() {
        dispatcher = new ConcurrentMessageDispatcher(threadPool);
        mpv3 = new MPv3(usm = new USM(DefaultSecurityProtocols.getInstance(),
                agent.getContextEngineID(),
                updateEngineBoots()));
        dispatcher.addMessageProcessingModel(new MPv1());
        dispatcher.addMessageProcessingModel(new MPv2c());
        dispatcher.addMessageProcessingModel(mpv3);
        initSnmpSession();
    }

    /**
     * Adds initial notification targets and filters.
     *
     * @param targetMIB       the SnmpTargetMIB holding the target configuration.
     * @param notificationMIB the SnmpNotificationMIB holding the notification (filter)
     *                        configuration.
     */
    @Override
    protected void addNotificationTargets(final SnmpTargetMIB targetMIB, final SnmpNotificationMIB notificationMIB) {
        targetMIB.addDefaultTDomains();
        vacmMIB.addViewTreeFamily(toOctetString("fullNotifyView"), new OID(prefix),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile);
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
                        toOctetString(notifyUser),
                        security.getUserSecurityLevel(notifyUser).getSnmpValue(),
                        StorageType.permanent);
        }
        else targetMIB.addTargetParams(NOTIFICATION_SETTINGS_TAG,
                MessageProcessingModel.MPv2c,
                SecurityModel.SECURITY_MODEL_SNMPv2c,
                toOctetString("cpublic"),
                SecurityLevel.AUTH_PRIV,
                StorageType.permanent);
        notificationMIB.addNotifyEntry(toOctetString("default"),
                toOctetString("notify"),
                SnmpNotificationMIB.SnmpNotifyTypeEnum.trap,
                StorageType.permanent);
    }



    /**
	 * Initializes SNMP transport.
	 */
	protected void initTransportMappings() throws IOException {
        try{
            final TransportMapping<?> tm = TransportMappings
                    .getInstance()
                    .createTransportMapping(GenericAddress.parse(String.format("%s/%s", hostName, port)));
            if(tm instanceof DefaultUdpTransportMapping)
                ((DefaultUdpTransportMapping)tm).setSocketTimeout(socketTimeout);
            transportMappings = new TransportMapping<?>[]{tm};
        }
        catch (final Exception e){
            throw new IOException(String.format("Unable to create SNMP transport for %s/%s address.", hostName, port), e);
        }
	}

    private boolean isDestroyed(){
        return agent == null;
    }

    /**
     * Updating of the gateway is finished.
     */
    @Override
    public synchronized void updated() {
        if (!isDestroyed())  //avoid updating of dead Agent
            run();
    }

    private void finishInit(final Iterable<? extends SnmpAttributeAccessor> attributes,
                            final Iterable<? extends SnmpNotificationMapping> notifications) throws DuplicateRegistrationException {
        for(final SnmpAttributeAccessor mapping: attributes)
            registerManagedObject(mapping);
        for(final SnmpNotificationMapping mapping: notifications)
            registerNotificationTarget(mapping);
        finishInit();
    }

    synchronized boolean start(final Iterable<? extends SnmpAttributeAccessor> attributes,
                  final Iterable<? extends SnmpNotificationMapping> notifications) throws IOException, DuplicateRegistrationException {
		switch (agentState){
            case STATE_STOPPED:
            case STATE_CREATED:
                init();
                if(coldStart) getServer().addContext(new OctetString("public"));
                finishInit(attributes, notifications);
                run();
                if(coldStart) sendColdStartNotification();
                coldStart = false;
            return true;
            default:
                SnmpHelpers.log(Level.SEVERE, "SNMP agent already started (state %s).", agentState, null);
            return false;
        }
	}

    /**
	 * Initializes SNMP communities.
	 */
	@SuppressWarnings("unchecked")
    protected void addCommunities(final SnmpCommunityMIB communityMIB) {
		final Variable[] com2sec = new Variable[] { toOctetString("public"), // community
																			// name
				toOctetString("cpublic"), // security name
				agent.getContextEngineID(), // local engine ID
				toOctetString("public"), // default context name
				new OctetString(), // transport tag
				new Integer32(StorageType.nonVolatile), // storage type
				new Integer32(RowStatus.active) // row status
		};
		final SnmpCommunityMIB.SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(
				toOctetString("public2public").toSubIndex(true), com2sec);
		communityMIB.getSnmpCommunityEntry().addRow(row);
	}

    @Override
    public void processNotification(final SnmpNotification wrappedNotification){
        final NotificationOriginator originator = super.notificationOriginator;
        if(originator != null){
            originator.notify(new OctetString(), wrappedNotification.notificationID, wrappedNotification.getBindings()); //for SNMPv3 sending
            originator.notify(new OctetString("public"), wrappedNotification.notificationID, wrappedNotification.getBindings()); //for SNMPv2 sending
        }
    }

    synchronized void suspend(){
        super.stop();
    }

    /**
     * Stops the agent by closing the SNMP session and associated transport
     * mappings.
     *
     * @since 1.1
     */
    @Override
    public synchronized void stop() {
        switch (agentState) {
            case STATE_CREATED:
            case STATE_RUNNING:
                suspend();
            case STATE_STOPPED:
                snmpTargetMIB.getSnmpTargetAddrEntry().removeAll();
                snmpTargetMIB.getSnmpTargetParamsEntry().removeAll();
                unregisterSnmpMIBs();
            default:

        }
    }

    @Override
    public synchronized void close() {
        stop();
        //destroy internal state
        snmpTargetMIB = null;
        agent = null;
        snmpv2MIB = null;
        snmpFrameworkMIB = null;
        snmpNotificationMIB = null;
        snmpProxyMIB = null;
        snmpCommunityMIB = null;
        snmp4jLogMIB = null;
        snmp4jConfigMIB = null;
        usmMIB = null;
        vacmMIB = null;
        server = null;
        session = null;
        transportMappings = null;
        dispatcher = null;
        mpv3 = null;
        usm = null;
        bootCounterFile = null;
        notificationOriginator = null;
        defaultProxyForwarder = null;
        defaultContext = null;
        defaultPersistenceProvider = null;
        configFileURI = null;
        sysDescr = null;
        sysOID = null;
        sysServices = null;
    }
}