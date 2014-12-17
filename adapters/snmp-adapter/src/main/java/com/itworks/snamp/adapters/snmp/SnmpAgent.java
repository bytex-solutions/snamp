package com.itworks.snamp.adapters.snmp;

import com.google.common.eventbus.Subscribe;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.ConcurrentMessageDispatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

/**
 * Represents SNMP Agent.
 * 
 * @author Roman Sakno, Evgeniy Kirichenko
 * 
 */
final class SnmpAgent extends BaseAgent implements SnmpNoitificationListener {
    private static final OctetString NOTIFICATION_SETTINGS_TAG = new OctetString("NOTIF_TAG");

	private final String hostName;
    private final int port;
    private final int socketTimeout;
    private boolean coldStart;
    private final Collection<SnmpAttributeMapping> attributes;
    private final Collection<SnmpNotificationMapping> notifications;
    private ExecutorService threadPool;
    private final SecurityConfiguration security;

    SnmpAgent(final int port,
                     final String hostName,
                     final SecurityConfiguration securityOptions,
                     final int socketTimeout) throws IOException {
		// These files does not exist and are not used but has to be specified
		// Read snmp4j docs for more info
		super(new File("conf.agent"), null,
                new CommandProcessor(
                        new OctetString(MPv3.createLocalEngineID())));
        coldStart = true;
        this.attributes = new ArrayList<>(10);
        this.notifications = new ArrayList<>(10);
        this.hostName = hostName;
        this.port = port;
        this.socketTimeout = socketTimeout;
        this.security = securityOptions;
	}

	@Override
	protected void registerManagedObjects() {
        for(final OID prefix: SnmpHelpers.getPrefixes(attributes)){
            boolean wasReadViewAdded = false;
            boolean wasWriteViewAdded = false;
            for(final SnmpAttributeMapping mo: SnmpHelpers.getObjectsByPrefix(prefix, attributes))
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
                    SnmpHelpers.log(Level.WARNING, "SNMP Internal Error. Call for SNMP developers.", e);
                }
        }
	}

    /**
     * Unregisters additional managed objects from the agent's server.
     */
    @Override
    protected final void unregisterManagedObjects() {
        for(final SnmpAttributeMapping mo: attributes)
            server.unregister(mo, null);
        for(final OID prefix: SnmpHelpers.getPrefixes(attributes)){
            vacmMIB.removeViewTreeFamily(new OctetString("fullReadView"), new OID(prefix));
            vacmMIB.removeViewTreeFamily(new OctetString("fullWriteView"), new OID(prefix));
        }
        attributes.clear();
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
        if (threadPool != null) {
            dispatcher = new ConcurrentMessageDispatcher(threadPool);
            mpv3 = new MPv3(agent.getContextEngineID().getValue());
            usm = new USM(SecurityProtocols.getInstance(),
                    agent.getContextEngineID(),
                    updateEngineBoots());
            SecurityModels.getInstance().addSecurityModel(usm);
            SecurityProtocols.getInstance().addDefaultProtocols();
            dispatcher.addMessageProcessingModel(new MPv1());
            dispatcher.addMessageProcessingModel(new MPv2c());
            dispatcher.addMessageProcessingModel(mpv3);
            initSnmpSession();
        } else super.initMessageDispatcher();
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
        if(notifications.isEmpty()) return;
        targetMIB.addDefaultTDomains();
        //register notifications
        for(final OID prefix: SnmpHelpers.getPrefixes(notifications)){
            vacmMIB.addViewTreeFamily(new OctetString("fullNotifyView"), new OID(prefix),
                    new OctetString(), VacmMIB.vacmViewIncluded,
                    StorageType.nonVolatile);
            for(final SnmpNotificationMapping mapping: SnmpHelpers.getObjectsByPrefix(prefix, notifications)){
                targetMIB.addTargetAddress(mapping.getReceiverName(),
                        mapping.getTransportDomain(),
                        mapping.getReceiverAddress(),
                        mapping.getTimeout(),
                        mapping.getRetryCount(),
                        new OctetString("notify"),
                        NOTIFICATION_SETTINGS_TAG,
                        StorageType.nonVolatile);
            }
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



    /**
	 * Initializes SNMP transport.
	 */
	protected void initTransportMappings() throws IOException {
        final TransportMappings mappings = TransportMappings.getInstance();
        try{
            TransportMapping<?> tm = mappings.createTransportMapping(GenericAddress.parse(String.format("%s/%s", hostName, port)));
            if(tm instanceof DefaultUdpTransportMapping)
                ((DefaultUdpTransportMapping)tm).setSocketTimeout(socketTimeout);
            transportMappings = new TransportMapping[]{tm};
        }
        catch (final RuntimeException e){
            throw new IOException(String.format("Unable to create SNMP transport for %s/%s address.", hostName, port), e);
        }
	}

    boolean start(final Collection<SnmpAttributeMapping> attrs,
                         final Collection<SnmpNotificationMapping> notifs,
                         final ExecutorService threadPool) throws IOException {
		switch (agentState){
            case STATE_STOPPED:
            case STATE_CREATED:
                this.threadPool = threadPool;
                attributes.addAll(attrs);
                notifications.addAll(notifs);
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
		final MOTableRow row = communityMIB.getSnmpCommunityEntry().createRow(
				new OctetString("public2public").toSubIndex(true), com2sec);
		communityMIB.getSnmpCommunityEntry().addRow(row);
	}

    @Subscribe
    @Override
    public void processNotification(final SnmpNotification wrappedNotification){
        if(notificationOriginator != null){
            notificationOriginator.notify(new OctetString(), wrappedNotification.notificationID, wrappedNotification.getBindings()); //for SNMPv3 sending
            notificationOriginator.notify(new OctetString("public"), wrappedNotification.notificationID, wrappedNotification.getBindings()); //for SNMPv2 sending
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
        switch (agentState){
            case STATE_RUNNING: super.stop();
            case STATE_STOPPED:
            case STATE_CREATED:
                snmpTargetMIB.getSnmpTargetAddrEntry().removeAll();
                snmpTargetMIB.getSnmpTargetParamsEntry().removeAll();
                unregisterSnmpMIBs();
            default:
                attributes.clear();
                notifications.clear();
                threadPool = null;
        }
    }
}