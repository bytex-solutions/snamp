package com.itworks.snamp.adapters.snmp;

import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.NotificationOriginator;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents SNMP Agent.
 * 
 * @author Roman Sakno, Evgeniy Kirichenko
 * 
 */
final class SnmpAgent extends BaseAgent implements NotificationOriginator {
    private static final OctetString NOTIFICATION_SETTINGS_TAG = new OctetString("NOTIF_TAG");
    private static final Logger logger = SnmpHelpers.getLogger();

	private final String hostName;
    private final int port;
    private final int socketTimeout;
    private boolean coldStart;
    private final Collection<SnmpAttributeMapping> attributes;
    private final Collection<SnmpNotificationMapping> notifications;
    private SecurityConfiguration security;

	public SnmpAgent(final int port, final String hostName, final SecurityConfiguration securityOptions, final int socketTimeout) throws IOException {
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
                    logger.log(Level.WARNING, "SNMP Internal Error. Call for SNMP developers.", e);
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

	public boolean start(final Collection<SnmpAttributeMapping> attrs, final Collection<SnmpNotificationMapping> notifs) throws IOException {
		switch (agentState){
            case STATE_STOPPED:
            case STATE_CREATED:
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
        }
    }

    /**
     * Sends notifications (traps) to all appropriate notification targets.
     * The targets to notify are determined through the SNMP-TARGET-MIB and
     * the SNMP-NOTIFICATION-MIB.
     *
     * @param context        the context name of the context on whose behalf this notification has
     *                       been generated.
     * @param notificationID the object ID that uniquely identifies this notification. For SNMPv1
     *                       traps, the notification ID has to be build using the rules provided
     *                       by RFC 2576.
     * @param vbs            an array of <code>VariableBinding</code> instances representing the
     *                       payload of the notification.
     * @return an array of ResponseEvent instances. Since the
     * <code>NotificationOriginator</code> determines on behalf of the
     * SNMP-NOTIFICTON-MIB contents whether a notification is sent as
     * trap/notification or as inform request, the returned array contains
     * an element for each addressed target, but only a response PDU for
     * inform targets.
     */
    @Override
    public Object notify(final OctetString context, final OID notificationID, final VariableBinding[] vbs) {
        return notificationOriginator != null ? notificationOriginator.notify(context, notificationID, vbs) : null;
    }

    /**
     * Sends notifications (traps) to all appropriate notification targets.
     * The targets to notify are determined through the SNMP-TARGET-MIB and
     * the SNMP-NOTIFICATION-MIB.
     *
     * @param context        the context name of the context on whose behalf this notification has
     *                       been generated.
     * @param notificationID the object ID that uniquely identifies this notification. For SNMPv1
     *                       traps, the notification ID has to be build using the rules provided
     *                       by RFC 2576.
     * @param sysUpTime      the value of the sysUpTime for the context <code>context</code>. This
     *                       value will be included in the generated notification as
     *                       <code>sysUpTime.0</code>.
     * @param vbs            an array of <code>VariableBinding</code> instances representing the
     *                       payload of the notification.
     * @return an array of ResponseEvent instances. Since the
     * <code>NotificationOriginator</code> determines on behalf of the
     * SNMP-NOTIFICTON-MIB contents whether a notification is sent as
     * trap/notification or as inform request, the returned array contains
     * an element for each addressed target, but only a response PDU for
     * inform targets.
     */
    @Override
    public Object notify(final OctetString context, final OID notificationID, final TimeTicks sysUpTime, final VariableBinding[] vbs) {
        return notificationOriginator != null ? notificationOriginator.notify(context, notificationID, sysUpTime, vbs) : null;
    }
}