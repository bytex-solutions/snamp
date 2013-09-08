package com.snamp.hosting;

import java.io.*;
import java.lang.management.ManagementFactory;

import javax.management.MalformedObjectNameException;

import com.snamp.connectors.jmx.*;

import org.snmp4j.TransportMapping;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.TransportMappings;

/**
 * SNMP Агент получающий данные из JMX
 * 
 * @author agrishin
 * 
 */
public final class Agent extends BaseAgent {
	private String address;
	private String[] oids;
	
	public Agent(final String address) throws IOException {

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
	 * @param monitor JMX-монитор для проброса через SNMP.
	 * @throws DuplicateRegistrationException 
	 */
	public void registerMonitor(final String prefix, final JmxMonitor monitor) throws DuplicateRegistrationException{
		this.getVacmMIB().addViewTreeFamily(new OctetString("fullReadView"), new OID(prefix),
				new OctetString(), VacmMIB.vacmViewIncluded,
				StorageType.nonVolatile);
		//пройтись по все атрибутам и зарегистрировать скаляр для значения каждого из атрибутов
		for(final String oid: monitor){
			final ManagedObject mo = JmxToSnmpValueConverter.createScalarValueProvider(oid, monitor.getAttribute(oid));
			if(mo == null) continue;
			else server.register(mo, null);
		}
	}

	/**
	 * Сохранить id процесса в файл чтобы в последствии иметь возможность
	 * обратиться к нему
	 */
	private static void savePid() throws IOException {
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		pid = pid.substring(pid.indexOf("[") == -1 ? 0 : pid.indexOf("[") + 1,
				pid.indexOf("@"));
		System.out.printf("Process ID: %s\n", pid);
		final File file = new File("jmx2snmp.pid");
		file.createNewFile();
		final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		try {
			bw.write(pid);
		} finally {
			bw.close();
		}
	}

	private static void runMainLoop() throws IOException, InterruptedException {
		// Программа выполняется пока не введена комманда "exit"
		while (true) {
			Thread.yield();
			Thread.sleep(2000);
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, MalformedObjectNameException, DuplicateRegistrationException {
		if (args.length < 1) {
			System.out.println("java jmx2snmp config-file");
			System.out.println("Example: java jmx2snmp mon.properties");
			return;
		}
		savePid();
		// Загружаем конфигурацию JMX и SNMP из файла
		final AgentStartupInfo settings = AgentStartupInfo.loadFromFile(args[0], ConfigurationFileFormat.parse(args[1]));
		final Agent ag = new Agent(String.format("%s/%s",
				settings.getHostInfo().getAddress(), settings.getHostInfo().getPort()));
		System.out.printf("SNMP-JMX Bridge Started at %s:%s\n", settings.getHostInfo().getAddress(), settings.getHostInfo().getPort());
		ag.start();
		// Получить объект для регистрации OIDов разных Jmx мониторов
		// Создаём JMX-мониторы
		for (final AgentStartupInfo.JmxServer sinfo : settings) {
			System.out.printf("Registering %s:%s, %s:%s\n", 
					sinfo.getRMIRegistry().getAddress(), 
					sinfo.getRMIRegistry().getPort(), 
					sinfo.getRMIServer().getAddress(), 
					sinfo.getRMIServer().getPort()
			);
			final JmxMonitor monitor;
			try{
				monitor = sinfo.createMonitor();
			}catch(IOException e){
				System.err.printf("Unable to register JMX-server(%s, %s). Reason: %s\n",
						sinfo.getRMIRegistry().getAddress(), sinfo.getRMIServer().getAddress(), e.getMessage());
				continue;
			}
			//Регистрируем привязки
			for(final AgentStartupInfo.JmxAttribute attr_: sinfo.getAttributes()){
				System.out.printf("Registering JMX object %s\n", attr_.getOwner() + attr_.getName());
				//к каждой привязке добавляем OID-префикс, что позволяет мониторить одни и те же MXBean, но на разных серверах
				monitor.addAttribute(attr_.getOwner(), attr_.getFullName(), String.format("%s.%s", sinfo.getOidPrefix(), attr_.getOidPostfix()), attr_.isUseRegExp());
			}
			//Регистрация OIDов разных Jmx мониторов
			ag.registerMonitor(sinfo.getOidPrefix(), monitor);
		}
		//регистрация процедуры остановки агента при выгрузке JVM
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			public void run() {
				ag.stop();
			}
		}));
		runMainLoop();
	}
}
;