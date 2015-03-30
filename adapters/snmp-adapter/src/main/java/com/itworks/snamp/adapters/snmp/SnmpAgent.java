package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.adapters.AttributeAccessor;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.NotificationOriginator;
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

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

/**
 * Represents SNMP Agent.
 * 
 * @author Roman Sakno, Evgeniy Kirichenko
 * 
 */
final class SnmpAgent extends BaseAgent implements SnmpNotificationListener {
    private static final OctetString NOTIFICATION_SETTINGS_TAG = new OctetString("NOTIF_TAG");

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

    void registerManagedObject(final SnmpAttributeMapping mo) throws DuplicateRegistrationException {
        mo.connect(server);
    }

    AttributeAccessor unregisterManagedObject(final SnmpAttributeMapping mo){
        return mo.disconnect(server);
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

    void unregisterNotificationTarget(final OctetString receiverName){
        getSnmpTargetMIB().removeTargetAddress(receiverName);
    }

    void unregisterNotificationTarget(final SnmpNotificationMapping mapping){
        unregisterNotificationTarget(mapping.getReceiverName());
        mapping.setNotificationOriginator(null);
    }

	@Override
	protected void registerManagedObjects() {
        vacmMIB.addViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.volatile_);
	}

    /**
     * Unregisters additional managed objects from the agent's server.
     */
    @Override
    protected final void unregisterManagedObjects() {
        vacmMIB.removeViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix));
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
    protected final void addNotificationTargets(final SnmpTargetMIB targetMIB, final SnmpNotificationMIB notificationMIB) {
        targetMIB.addDefaultTDomains();
        vacmMIB.addViewTreeFamily(new OctetString("fullNotifyView"), new OID(prefix),
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
            transportMappings = new TransportMapping[]{tm};
        }
        catch (final Exception e){
            throw new IOException(String.format("Unable to create SNMP transport for %s/%s address.", hostName, port), e);
        }
	}

    void suspend(){
        super.stop();
    }

    void resume(){
        run();
    }

    boolean start() throws IOException {
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
                SnmpHelpers.log(Level.SEVERE, "SNMP agent already started (state %s).", agentState, null);
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
		final SnmpCommunityMIB.SnmpCommunityEntryRow row = communityMIB.getSnmpCommunityEntry().createRow(
				new OctetString("public2public").toSubIndex(true), com2sec);
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

    /**
     * Stops the agent by closing the SNMP session and associated transport
     * mappings.
     *
     * @since 1.1
     */
    @Override
    public void stop() {
        switch (agentState) {
            case STATE_RUNNING:
                threadPool.shutdownNow();
                suspend();
            case STATE_STOPPED:
            case STATE_CREATED:
                snmpTargetMIB.getSnmpTargetAddrEntry().removeAll();
                snmpTargetMIB.getSnmpTargetParamsEntry().removeAll();
                unregisterSnmpMIBs();
        }
    }
}