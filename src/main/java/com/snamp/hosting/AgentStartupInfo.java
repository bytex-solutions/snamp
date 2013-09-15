package com.snamp.hosting;

import java.io.*;
import java.util.*;
import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXServiceURL;

import com.snamp.connectors.ManagementConnector;
import com.snamp.connectors.ManagementConnectorFactory;
import com.snamp.connectors.jmx.JmxConnectorFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Класс, содержащий параметры запуска агента из файла конфигурации.
 * 
 * @author rvsakno
 * 
 */
final class AgentStartupInfo implements Iterable<AgentStartupInfo.JmxServer> {

	/**
	 * Класс описывающий поля типа RMIRegistry или RMIServer.
	 * Содержит описание хоста для подключения - адрес и порт.
	 * Не используется для схожей структуры SnmpHost, т.к. могут в 
	 * дальнейшем появиться дополнительные параметры запуска сервиса.
	 * Модификатор доступа public выставлен для корректной работы библиотеки
	 * org.yaml.snakeyaml.Yaml, чей парсер YAML файлов не видит приватных классов.
	 * 
	 * @author erkirichenko
	 *
	 */
	public static class ConnectionInfo implements Serializable{
		private static final long serialVersionUID = 9153610709012097653L;
		private String address;
		private String port;

	    /**
	     * {@link ConnectionInfo#getPort}
	     */
		public void setPort(final String port) {
			this.port = port;
		}
		
		/**
		 * Порт хоста
		 */
		public String getPort() {
			return port;
		}

	    /**
	     * {@link ConnectionInfo#getAddress}
	     */
		public void setAddress(final String address) {
			this.address = address;
		}
		
		/**
		 * IP адрес хоста
		 */	
		public String getAddress() {
			return address;
		}
	};

	/**
	 * Класс для объявления параметров запуска моста. 
	 * @todo: в случае необходимости дополнить внутреннюю структуру
	 * 
	 * @author erkirichenko
	 *
	 */
	public final static class SnmpHost extends ConnectionInfo {
		private static final long serialVersionUID = -9092934054149477135L;
	}

	/**
	 * Описывает метаструктуру для десериализации класса из YAML файла
	 * 
	 * @author erkirichenko
	 *
	 */
	public final static class YamlDocumentRoot implements Serializable {
		private static final long serialVersionUID = 8616395189947612585L;
		private SnmpHost snmpHost;
		private List<JmxServer> jmxServer;

	    /**
	     * {@link YamlDocumentRoot#getJmxServer}
	     */
		public void setJmxServer(List<JmxServer> jmxServer) {
			this.jmxServer = jmxServer;
		}

		/**
		 * Массив connectors серверов
		 * @return
		 */
		public List<JmxServer> getJmxServer() {
			return jmxServer;
		}
		
	    /**
	     * {@link YamlDocumentRoot#getSnmpHost}
	     */
		public void setSnmpHost(SnmpHost snmpHost) {
			this.snmpHost = snmpHost;
		}
		
		/**
		 * Настройки хоста, используется {@link ConnectionInfo}
		 * @return
		 */
		public SnmpHost getSnmpHost() {
			return snmpHost;
		}
	}
	
	/**
	 * Класс для привязки атрибутов MXBean к SNMP OID.
	 * Вынесен наверх и имеет модификатор public по тем же причинам, что и 
	 * класс {@link ConnectionInfo ConnectionInfo}
	 * 
	 * @author erkirichenko
	 *
	 */
	public final static class JmxAttribute implements Serializable {
		
		private static final long serialVersionUID = 3549213735560557965L;
		public static final char delimiter = '@';
		
		private String name = ""; // required
		private String oidPostfix = ""; // required
		private String owner = ""; // required
		private String[] fieldPath = new String[0]; // optional
		private boolean useRegExp = false; // optional	

	    /**
	     * {@link JmxAttribute#getName}
	     */
		public void setName(String attribute) {
			this.name = attribute;
		}

		/**
		 * Имя сервера
		 * @return
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Получить полное наименование JMX аттрибута.
		 * Включает в себя имя аттрибута и доп. аттрибуты через разделитель
		 * 
		 * @return
		 */
		public String getFullName(){
			return getName() + (fieldPath != null && fieldPath.length > 0 ? new String(new char[]{delimiter}) + getFieldPath() : "");
		}
		
	    /**
	     * {@link JmxAttribute#getFullName}
	     */
		public void setOidPostfix(String oidPostfix) {
			this.oidPostfix = oidPostfix;
		}

		/**
		 * Получить Oid постфикс для snmp аттрибута
		 * @return
		 */
		public String getOidPostfix() {
			return oidPostfix;
		}
		
	    /**
	     * {@link JmxAttribute#getOwner}
	     */
		public void setOwner(String owner) {
			this.owner = owner;
		}

		/**
		 * Получить основную часть JMX аттрибута 
		 * <p> Например для аттрибута java.lang:type=OperatingSystem.SystemLoadAverage 
		 * основной частью будет java.lang:type=OperatingSystem
		 * 
		 * @return
		 */
		public String getOwner() {
			return owner;
		}
		
		/**
		 * Установка {@link JmxAttribute#getFieldPath}.
		 * @param path
		 * @param delimiter
		 */
		public void setFieldPath(final String path, final String delimiter){
			fieldPath = path != null && path.length() > 0 ? path.split(delimiter) : new String[0];
		}
		
		/**
		 * Вспомогательная перегруженная функция.
		 * @param path
		 * @param delimiter
		 */
		public void setFieldPath(final String path, final char delimiter){
			setFieldPath(path, new String(new char[]{delimiter}));
		}

	    /**
	     * {@link JmxAttribute#getFieldPath}
	     */
		public void setFieldPath(final String path) {
			setFieldPath(path, "/");
		}

		/**
		 * Структуру аттрибутов в виде строки.
		 * Добавочные JMX аттрибуты хранятся в виде массива строк.
		 * @return
		 */
		public String getFieldPath() {
			final StringBuilder result = new StringBuilder();
			for(int i = 0; i < fieldPath.length; i++){
				result.append(fieldPath[i]);
				if(i < fieldPath.length - 1) result.append(delimiter);
			}
			return result.toString();
		}

		/**
		 * Является ли имя Beam'а регулярным выражением
		 * @return
		 */
		public boolean isUseRegExp() {
			return useRegExp;
		}

		/**
		 * Установка {@link JmxAttribute}.
		 * @param useRegExp
		 */
		public void setUseRegExp(boolean useRegExp) {
			this.useRegExp = useRegExp;
		}

	}

	/**
	 * Описание сервера JMX с необходимыми полями,
	 * а также привязанными аттрибутами  {@link JmxAttribute JmxAttribute}
	 * 
	 * @author erkirichenko
	 *
	 */
	public final static class JmxServer {
		private String name; // required
		private ConnectionInfo RMIServer; // required
		private ConnectionInfo RMIRegistry; // required
		private String login; // required
		private String password; // required
		private String oidPrefix; // required
		private List<JmxAttribute> attributes; // required
		
		/**
		 * Конструктор по умолчанию
		 */
		public JmxServer() {
			name = "";
			RMIServer = new ConnectionInfo();
			RMIServer.setAddress("0.0.0.0");
			RMIServer.setPort("");

			RMIRegistry = new ConnectionInfo();
			RMIRegistry.setAddress("0.0.0.0");
			RMIRegistry.setPort("");

			login = "";
			password = "";
			oidPrefix = "";
		};

		/**
		 * Конструктор с использованием полных аргументов
		 */
		public JmxServer(final String JxmName,
				final String JxmRMIServerAddress,
				final String JxmRMIServerPort,
				final String JxmRMIRegistryAddress,
				final String JxmRMIRegistryPort, final String jmxlogin,
				final String jmxpass, final String oidpref) {
			name = "JxmName";
			RMIServer = new ConnectionInfo();
			RMIServer.setAddress(JxmRMIServerAddress);
			RMIServer.setPort(JxmRMIServerPort);

			RMIRegistry = new ConnectionInfo();
			RMIRegistry.setAddress(JxmRMIRegistryAddress);
			RMIRegistry.setPort(JxmRMIRegistryPort);

			login = jmxlogin;
			password = jmxpass;
			oidPrefix = oidpref;
		}

		
	    /**
	     * {@link JmxServer#getName}
	     */		
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Название сервера
		 * @return
		 */
		public String getName() {
			return name;
		}
		
	    /**
	     * {@link JmxServer#getRMIRegistry}
	     */
		public void setRMIRegistry(ConnectionInfo rMIRegistry) {
			RMIRegistry = rMIRegistry;
		}

		/**
		 * RMI регистер (структура типа {@link ConnectionInfo ConnectionInfo})
		 * @return
		 */
		public ConnectionInfo getRMIRegistry() {
			return RMIRegistry;
		}

	    /**
	     * {@link JmxServer#getRMIServer}
	     */
		public void setRMIServer(ConnectionInfo rMIServer) {
			RMIServer = rMIServer;
		}

		/**
		 * RMI сервер (структура типа {@link ConnectionInfo ConnectionInfo})
		 * @return
		 */
		public ConnectionInfo getRMIServer() {
			return RMIServer;
		}

	    /**
	     * {@link JmxServer#getLogin}
	     */
		public void setLogin(String login) {
			this.login = login;
		}

		/**
		 * Логин на сервер
		 * @return
		 */
		public String getLogin() {
			return login;
		}

	    /**
	     * {@link JmxServer#getOidPrefix}
	     */
		public void setOidPrefix(String oidPrefix) {
			this.oidPrefix = oidPrefix;
		}

		/**
		 * OID префикс JMX сервера
		 * @return
		 */
		public String getOidPrefix() {
			return oidPrefix;
		}

	    /**
	     * {@link JmxServer#getPassword}
	     */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * Пароль на сервер
		 * @return
		 */
		public String getPassword() {
			return password;
		}
		
	    /**
	     * {@link JmxServer#getAttributes}
	     */
		public void setAttributes(List<JmxAttribute> attributes) {
			this.attributes = Collections.unmodifiableList(attributes);
		}
		
		/**
		 * Аттрибуты, закрепленные за JMX сервером
		 * @return
		 */
		public List<JmxAttribute> getAttributes() {
			return attributes;
		}

		/**
		 * Создать JMX-монитор без привязок JMX-атрибутов к OID.
		 * 
		 * @return Новый пустой монитор.
		 * @throws MalformedObjectNameException
		 * @throws IOException
		 */
		public ManagementConnector createMonitor() throws IOException,
				MalformedObjectNameException {
            final String connectionFormat = "service:jmx:rmi://%s:%s/jndi/rmi://%s:%s/jmxrmi";
            final ManagementConnectorFactory factory = new JmxConnectorFactory();
            return factory.newInstance(String.format(connectionFormat, RMIServer.getAddress(), RMIServer.getPort(), RMIRegistry.getAddress(), RMIRegistry.getPort()), new Properties(){{
                put("login", login);
                put("password", password);
            }});
		}
	};

	/**
	 * Объект, содержащий массив JMX серверов
	 */
	private final List<JmxServer> _servers;
	
	/**
	 * Информация о настройках хоста для JMX-SNMP хоста.
	 * Пока является экземпляром {@link ConnectionInfo ConnectionInfo}
	 */
	private final SnmpHost _hostInfo;
	

	private static String getProperty(Properties prop, String format,
			Object... args) {
		return prop.getProperty(String.format(format, args));
	}

	/**
	 * Создать поставщика параметров запуска агента из набора свойств YAML.
	 * 
	 * @param hostInfo структура {@link ConnectionInfo ConnectionInfo} c информаций о настройках моста
 	 * @param servers коллекция JMX серверов
	 */
	public AgentStartupInfo(final SnmpHost hostInfo,
			final List<JmxServer> servers) {
		if (hostInfo == null)
			throw new NullPointerException("hostInfo is null");
		if (servers == null)
			throw new NullPointerException("servers is null");
		_hostInfo = hostInfo;
		_servers = Collections.unmodifiableList(servers);
	}

	/**
	 * Фабрика для создания настроек в зависимости от типа файла настроек
	 * @see ConfigurationFileFormat
	 * @param fileName Путь к конфигурационному файлу
	 * @return AgentStartupInfo экзампляр настроек
	 * @throws IOException
	 */
	private static AgentStartupInfo createFromJavaProperties(
			final String fileName) throws IOException {
		List<JmxServer> _servers; // to return
		final SnmpHost _hostInfo = new SnmpHost(); // to return

		final Properties result = new Properties();
		final InputStream fis = new FileInputStream(fileName);
		try {
			result.load(fis);
		} finally {
			fis.close();
		}
		final List<JmxServer> infos = new ArrayList<JmxServer>();
		final String JmxServerName = "JmxServerName%s";
		final String JxmRMIServerAddress = "JxmRMIServerAddress%s";
		final String JxmRMIServerPort = "JxmRMIServerPort%s";
		final String JxmRMIRegistryAddress = "JxmRMIRegistryAddress%s";
		final String JxmRMIRegistryPort = "JxmRMIRegistryPort%s";
		final String JmxServerLogin = "JmxServerLogin%s";
		final String JmxServerPassword = "JmxServerPassword%s";
		final String OidPrefix = "OidPrefix%s";

		_hostInfo.setAddress(result.getProperty("SnmpHostAddress"));
		_hostInfo.setPort(result.getProperty("SnmpHostPort"));
		for (Integer i = 0; ((Properties) result).containsKey(String.format(
				JmxServerName, i)); i++)
			infos.add(new JmxServer(JmxServerName, getProperty(result,
					JxmRMIServerAddress, i), getProperty(result,
					JxmRMIServerPort, i), getProperty(result,
					JxmRMIRegistryAddress, i), getProperty(result,
					JxmRMIRegistryPort, i), getProperty(result, JmxServerLogin,
					i), getProperty(result, JmxServerPassword, i), getProperty(
					result, OidPrefix, i)));
		_servers = Collections.unmodifiableList(infos);

		List<JmxAttribute> attributes_ = new ArrayList<JmxAttribute>();

		for (final Object key : result.keySet()) {
			final String path = key.toString();
			final int lastdot = path.lastIndexOf(".");
			if (lastdot < 0) continue;
			// разбиваем ключ на название класса и атрибута
			final String beanClass = path.substring(0, lastdot);
			final String beanAttribute = path.substring(lastdot + 1,
					path.length());

			JmxAttribute attr_ = new JmxAttribute();
			attr_.setOwner(beanClass);
			attr_.setName(beanAttribute);
			attr_.setOidPostfix(((Properties) result).get(path).toString());

			// убираем field'ы на конце аттрибута, добавляем их если нужно как поле
			final int lastat = beanClass.indexOf(JmxAttribute.delimiter);
			if (lastat >= 0) {
				attr_.setFieldPath(beanClass.substring(lastat, beanClass.length()), JmxAttribute.delimiter);
				attr_.setOwner(beanClass.substring(0, lastat));
			};
			attributes_.add(attr_); // вставили в список
		}

		// если нашлись аттрибуты - привязываем КО ВСЕМ серверам (т.к. JavaProperties)
		if (attributes_.size() > 0) 
			for (JmxServer current : _servers)
				current.setAttributes(attributes_);
		
		/* Раннее использовалось для создания валидного файла настроек
		 * Representer representer = new Representer();
		 * representer.addClassTag(yamlOptions.class, new Tag("!options")); Yaml
		 * yaml = new Yaml(); yamlOptions opt_ = new
		 * yamlOptions(_hostInfo,_servers);
		 * System.out.println(opt_.getSnmpHost());
		 * System.out.println(yaml.dump(opt_));
		 */
		return new AgentStartupInfo(_hostInfo, _servers);
	}

	private static AgentStartupInfo createFromYaml(final String fileName)
			throws IOException {
		final Yaml yaml = new Yaml();
		final YamlDocumentRoot opt = yaml.loadAs(new FileInputStream(new File(fileName)),
				YamlDocumentRoot.class);
		return new AgentStartupInfo(opt.getSnmpHost(), opt.getJmxServer());
	}

	/**
	 * Загрузить из файла настройку моста JMX-snmp
	 * 
	 * @param fileName
	 * @param format
	 * @return
	 * @throws IOException
	 */
	public static AgentStartupInfo loadFromFile(final String fileName,
			final ConfigurationFileFormat format) throws IOException {
		switch (format) {
		case PROPERTIES:
			return createFromJavaProperties(fileName);
		case YAML:
			return createFromYaml(fileName);
		default:
			throw new IllegalArgumentException(String.format(
					"Invalid file format %s", format));
		}
	}

	
	public Iterator<JmxServer> iterator() {
		return _servers.iterator();
	}

	public SnmpHost getHostInfo() {
		return _hostInfo;
	}
}
