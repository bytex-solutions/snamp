package com.snamp.hosting;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

import com.snamp.connectors.ManagementConnector;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.TransportMappings;

/**
 * SNMP Агент получающий данные из JMX
 * 
 * @author agrishin
 * 
 */
public final class SnmpAgent extends BaseAgent {
	private String address;
	
	public SnmpAgent(final String address) throws IOException {

		// These files does not exist and are not used but has to be specified
		// Read snmp4j docs for more info
		super(new File("conf.agent"), new File("bootCounter.agent"),
				new CommandProcessor(
						new OctetString(MPv3.createLocalEngineID())));
		this.address = address;
	}

	
	protected void registerManagedObjects() {
	}

	/**
	 * Регистирует объект в SNMP агенте
	 */
	public void registerManagedObject(ManagedObject mo) {
		try {
			server.register(mo, null);
		} catch (DuplicateRegistrationException ex) {
			throw new RuntimeException(ex);
		}
	}

	public void unregisterManagedObject(MOGroup moGroup) {
		moGroup.unregisterMOs(server, getContext(moGroup));
	}

	
	protected void addNotificationTargets(SnmpTargetMIB targetMIB,
			SnmpNotificationMIB notificationMIB) {
	}

	/**
	 * Доступ видимости (Minimal View based Access Control)
	 * http://www.faqs.org/rfcs/rfc2575.html
	 */
	
	protected void addViews(VacmMIB vacm) {

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
	 * Нужно только для SNMPv3
	 */
	protected void addUsmUser(USM usm) {
	}

	/**
	 * Инициализирует SNMP монитор адрес
	 */
	protected void initTransportMappings() throws IOException {
		transportMappings = new TransportMapping[1];
		Address addr = GenericAddress.parse(address);
		TransportMapping tm = TransportMappings.getInstance()
				.createTransportMapping(addr);
		transportMappings[0] = tm;
	}

	/**
	 * Стартует SNMP агент
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		init();
		addShutdownHook();
		getServer().addContext(new OctetString("public"));
		finishInit();
		run();
		sendColdStartNotification();
	}

	protected void unregisterManagedObjects() {
		// here we should unregister those objects previously registered...
	}

	/**
	 * Кнфигурирует публичный доступ
	 */
	protected void addCommunities(SnmpCommunityMIB communityMIB) {
		Variable[] com2sec = new Variable[] { new OctetString("public"), // community
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
	
	/**
	 * Зарегистрировать JMX-монитор для проброса через SNMP.
	 * @param objects JMX-монитор для проброса через SNMP.
	 * @throws DuplicateRegistrationException 
	 */
	public void registerManagedObjects(final String prefix, final Iterable<ManagedObject> objects) throws DuplicateRegistrationException{
		this.getVacmMIB().addViewTreeFamily(new OctetString("fullReadView"), new OID(prefix),
				new OctetString(), VacmMIB.vacmViewIncluded,
				StorageType.nonVolatile);
		//пройтись по все атрибутам и зарегистрировать скаляр для значения каждого из атрибутов
		for(final ManagedObject mo: objects)
			server.register(mo, null);
	}


}