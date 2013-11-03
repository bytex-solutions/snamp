package com.snamp.adapters;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.snamp.connectors.ManagementConnector;
import net.xeoh.plugins.base.annotations.*;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;
import com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

/**
 * Represents SNMP Agent.
 * 
 * @author agrishin
 * 
 */
@PluginImplementation
final class SnmpAdapter extends SnmpAdapterBase {
    /**
     * Represents a collection of MO's with OID postfixes.
     */
    private static final class ManagementAttributes extends HashMap<String, ManagedObject>{
        public ManagementAttributes(){
            super(5);
        }
    }

	private String address;
    private int port;
    private boolean coldStart;
    private final Map<String, ManagementAttributes> attributes;
	
	public SnmpAdapter() throws IOException {
		// These files does not exist and are not used but has to be specified
		// Read snmp4j docs for more info
		super(new File("conf.agent"), null,
				new CommandProcessor(
						new OctetString(MPv3.createLocalEngineID())));
        coldStart = true;
        port = defaultPort;
        address = defaultAddress;
        attributes = new HashMap<>();
	}

    private static void registerManagedObjects(final MOServer server, final VacmMIB mib, final String prefix, Iterable<ManagedObject> mos){
        mib.addViewTreeFamily(new OctetString("fullReadView"), new OID(prefix),
                new OctetString(), VacmMIB.vacmViewIncluded,
                StorageType.nonVolatile);
        for(final ManagedObject mo: mos)
            try {
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
    protected void unregisterManagedObjects() {
        for(final String prefix: attributes.keySet()){
            final ManagementAttributes attrs = attributes.get(prefix);
            for(final String postfix: attrs.keySet()){
                final ManagedObject mo = server.getManagedObject(new OID(combineOID(prefix, postfix)), null);
                if(mo != null) server.unregister(mo, null);
            }
        }
    }

    /**
	 * Доступ видимости (Minimal View based Access Control)
	 * http://www.faqs.org/rfcs/rfc2575.html
	 */
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
	}

	/**
	 * Initializes SNMPv3 users.
	 */
	protected void addUsmUser(final USM usm) {
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
	 * Initializes SNMP transport.
	 */
	protected void initTransportMappings() throws IOException {
		final TransportMapping tm = TransportMappings.getInstance()
				.createTransportMapping(GenericAddress.parse(String.format("%s/%s", address, port)));
		transportMappings = new TransportMapping[]{tm};
	}

	private void start() throws IOException {
		init();
        if(coldStart) getServer().addContext(new OctetString("public"));
		finishInit();
		run();
        if(coldStart) sendColdStartNotification();
        coldStart = false;
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

    private boolean start(final Integer port, final String address) throws IOException{
        this.port = port != null ? port.intValue() : defaultPort;
        this.address = address != null && address.length() > 0 ? address : defaultAddress;
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
                return start(Integer.valueOf(parameters.get(portParamName)), parameters.get(addressParamName));
            default:return false;
        }
    }

    /**
     * Stops the connector hosting.
     *
     * @param saveAttributes {@literal true} to save previously exposed attributes for reuse; otherwise,
     *                       clear internal list of exposed attributes.
     * @return {@literal true}, if adapter is previously started; otherwise, {@literal false}.
     */
    @Override
    public boolean stop(final boolean saveAttributes) {
        switch (agentState){
            case STATE_RUNNING:
                super.stop();
                if(!saveAttributes) attributes.clear();
                return true;
            default:return false;
        }
    }

    private void exposeAttribute(final ManagementConnector connector, final String prefix, final String postfix, AttributeConfiguration attribute){
        final String oid = combineOID(prefix, postfix);
        final ManagedObject mo = SnmpType.createManagedObject(connector, oid, attribute.getAttributeName(), attribute.getAdditionalElements(), attribute.getReadWriteTimeout());
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
    public void exposeAttributes(final ManagementConnector connector, final String namespace, final Map<String, AttributeConfiguration> attributes) {
        for(final String postfix: attributes.keySet())
            exposeAttribute(connector, namespace, postfix, attributes.get(postfix));
    }

    /**
     * Releases all resources associated with this adapter.
     */
    @Override
    public void close() {
        super.stop();
    }
}