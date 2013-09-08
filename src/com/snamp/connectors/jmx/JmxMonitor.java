package com.snamp.connectors.jmx;

import java.io.*;
import java.util.*;

import javax.management.*;
import javax.management.remote.*;
import javax.management.openmbean.*;


/**
 * Предоставляет параметры мониторинга WSO2 ESB.
 * @author agrishin
 * 
 */
final class JmxMonitor implements Iterable<String> {
	/**
	 * Разделитель составного атрибута, используется в имени атрибута.
	 */
	public static final char delimiter = '@'; 
	public static final char regExDelimiter = '*';
	
	private static final String connectionFormat = "service:jmx:rmi://%s:%s/jndi/rmi://%s:%s/jmxrmi";
	private final Map<String, AttributeProvider> _attributes;
	private final JMXServiceURL _url;
	private final Map<String, ?> _connectionParameters;
	
	/**
	 * Функциональный интерфейс для обработки JMX-подключения.
	 * @author rvsakno
	 *
	 * @param <T> Тип результата обработки соединения.
	 */
	protected interface MBeanServerConnectionHandler<T>
	{
		/**
		 * Обработать JMX-соединение и вернуть результат обработки.
		 * @param connection
		 * @return
		 */
		T handle(MBeanServerConnection connection) throws IOException, JMException;
		
		/**
		 * Получить значение по-умлочанию, если произошёл сбой в канале.
		 * @param e Описание сбоя в JMX-подключении.
		 * @return
		 */
		T getDefaultValue(Exception e);
	}
	
	/**
	 * Абстрактный класс для построения обработчиков JMX-соединения.
	 * @author rvsakno
	 *
	 * @param <T>
	 */
	protected abstract class DefaultServerConnectionHandler<T> implements MBeanServerConnectionHandler<T>
	{
		
		public T getDefaultValue(Exception e) {
			return null;
		}
	}
	
	/**
	 * Навигатор по составному значению JMX-атрибута, формат имени его атрибут@поле1@поле2.
	 * @author rvsakno
	 *
	 */
	private static final class CompositeValueNavigator
	{
		public final String attributeName;
		private final String[] path;
		
		public CompositeValueNavigator(final String attributeName)
		{
			if(!isCompositeAttribute(attributeName)) throw new IllegalArgumentException("Неверный формат имени составного атрибута");
			final String[] parts = attributeName.split(new String(new char[]{delimiter}));
			this.attributeName = parts[0];
			this.path = Arrays.copyOfRange(parts, 1, parts.length);
		}
		
		/**
		 * Глубина вложенности дочерних атрибутов.
		 * @return
		 */
		public int depth(){
			return path.length;
		}
		
		/**
		 * Получить имя вложенного атрибута.
		 * @param index
		 * @return
		 */
		public String item(int index)
		{
			return path[index];
		}
		
		private Object getValue(final Object root, final int index)
		{
			if(root instanceof CompositeData && index < path.length){
				final CompositeData cdata = (CompositeData)root;
				final String subattr = path[index];
				return cdata.containsKey(subattr) ? getValue(cdata.get(subattr), index + 1) : root;
			}
			else return root;
		}
		
		/**
		 * Получить значение вложенного атрибута.
		 * @param root
		 * @return
		 */
		public Object getValue(final Object root)
		{
			return getValue(root, 0);
		}
		
		private String getType(final Object root, final int index)
		{
			if(root instanceof CompositeType && index < path.length){
				final CompositeType cdata = (CompositeType)root;
				final String subattr = path[index];
				return cdata.containsKey(subattr) ? getType(cdata.getType(subattr).getClassName(), index + 1) : root.toString();
			}
			else return root.toString();
		}
		
		/**
		 * Получить тип вложенного атрибута.
		 * @param root
		 * @return
		 */
		public String getType(final OpenType<?> root)
		{
			return getType(root, 0);
		}
		
		/**
		 * Получить полный путь композитного атрибута.
		 */
		
		public String toString()
		{
			return this.attributeName + Arrays.toString(path).replace(", ", new String(new char[]{delimiter}));
		}
		
		public static boolean isCompositeAttribute(final String attributeName)
		{
			return attributeName.indexOf(delimiter) >= 0;
		}
	}
	
	/**
	 * Поставщик значения JMX-атрибута
	 * @author rvsakno
	 *
	 */
	public static interface AttributeProvider extends Set<Object>
	{
		/**
		 * Получить каноническое имя типа атрибута.
		 * @return Каноническое имя типа атрибута.
		 */
		public String getAttributeClassName();
		
		/**
		 * Получить значение атрибута.
		 * @param defaultValue Значение по-умолчанию, если атрибут не доступен
		 * @return
		 */
		public Object getValue(final Object defaultValue);
		
		/**
		 * Получить имя объекта, который владеет данным атрибутом.
		 * @return
		 */
		public ObjectName getOwner();
	}
	
	/**
	 * Поставщик значения регэкспового JMX-атрибута.
	 * @author erkirichenko
	 *
	 */
	private class RegExAttributeProvider extends HashSet<Object> implements AttributeProvider{
		private static final long serialVersionUID = -8073701091836613975L;
		private final ObjectName beanName;
		private final String attributeClass;
		private final String attributeName;
		
		public RegExAttributeProvider(final ObjectName oname, final String attributeName, final Collection<? extends Object> attrs) throws IOException, OperationsException, ReflectionException{
			super(attrs != null ? attrs : new ArrayList<Object>());
			if(oname == null) throw new NullPointerException("oname is null");
			beanName = oname;
			this.attributeName = attributeName;
			
			// Получить список всех подходящих под паттерн bean'ов, пробежать по ним и вернуть подходящий аттрибут.
			// Пометка - если под паттерн подойдут несколько beam'ов, вернут будет лишь первый!
			final MBeanAttributeInfo targetAttr = handleConnection(new DefaultServerConnectionHandler<MBeanAttributeInfo>() {
				
				public MBeanAttributeInfo handle(MBeanServerConnection connection) throws IOException, JMException {
					
					Set<ObjectInstance> beans = connection.queryMBeans(oname,null);

					for( ObjectInstance instance : beans )
					{
					    MBeanInfo info = connection.getMBeanInfo( instance.getObjectName() );
					    for(final MBeanAttributeInfo attr: info.getAttributes())
							if(attributeName.equals(attr.getName())) return attr;
						return null;
					}
					return null;
				}
			});

			if(targetAttr == null) throw new AttributeNotFoundException(String.format("JMX attribute %s doesn't exist", attributeName));
			this.attributeClass = targetAttr.getType();
		}
		
		/**
		 * Получить текстовое название типа атрибута.
		 * @return
		 */
		
		public String getAttributeClassName()
		{
			return attributeClass;
		}
		
		/**
		 * Получить значение JMX-атрибута.
		 * @return Значение атрибута.
		 * @throws OperationsException
		 * @throws JMException
		 * @throws OperationsException
		 */
		
		public Object getValue(final Object defaultValue){
			return handleConnection(new MBeanServerConnectionHandler<Object>(){
				
				public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
					return connection.getAttribute(beanName, attributeName);
				}

				
				public Object getDefaultValue(Exception e) {
					return defaultValue;
				}
			});
		}

		
		public ObjectName getOwner() {
			return beanName;
		}
	}
	
	
	/**
	 * Поставщик значения примитивного JMX-атрибута.
	 * @author rvsakno
	 *
	 */
	private final class PlainAttributeProvider extends HashSet<Object> implements AttributeProvider{
		private static final long serialVersionUID = -8073701091836613975L;
		private final ObjectName beanName;
		private final String attributeClass;
		private final String attributeName;
		
		public PlainAttributeProvider(final ObjectName oname, final String attributeName, final Collection<? extends Object> attrs) throws IOException, OperationsException, ReflectionException{
			super(attrs != null ? attrs : new ArrayList<Object>());
			if(oname == null) throw new NullPointerException("oname is null");
			beanName = oname;
			this.attributeName = attributeName;
			//получить описатель поля, этот описатель может содержать знак @ для вложенного атрибута
			final MBeanAttributeInfo targetAttr = handleConnection(new DefaultServerConnectionHandler<MBeanAttributeInfo>() {
				
				public MBeanAttributeInfo handle(MBeanServerConnection connection) throws IOException, JMException {
					for(final MBeanAttributeInfo attr: connection.getMBeanInfo(oname).getAttributes())
						if(attributeName.equals(attr.getName())) return attr;
					return null;
				}
			});
			if(targetAttr == null) throw new AttributeNotFoundException(String.format("JMX attribute %s doesn't exist", attributeName));
			this.attributeClass = targetAttr.getType();
		}
		
		/**
		 * Получить текстовое название типа атрибута.
		 * @return
		 */
		
		public String getAttributeClassName()
		{
			return attributeClass;
		}
		
		/**
		 * Получить значение JMX-атрибута.
		 * @return Значение атрибута.
		 * @throws OperationsException
		 * @throws JMException
		 * @throws OperationsException
		 */
		
		public Object getValue(final Object defaultValue){
			return handleConnection(new MBeanServerConnectionHandler<Object>(){
				
				public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
					return connection.getAttribute(beanName, attributeName);
				}

				
				public Object getDefaultValue(Exception e) {
					return defaultValue;
				}
			});
		}

		
		public ObjectName getOwner() {
			return beanName;
		}
	}
	
	/**
	 * Поставщик значения композитного JMX-атрибута.
	 * @author rvsakno
	 *
	 */
	private final class CompositeAttributeProvider extends HashSet<Object> implements AttributeProvider{
		private static final long serialVersionUID = -8073701091836613975L;
		public final ObjectName beanName;
		private final CompositeValueNavigator navigator;
		private final OpenType<?> compositeType;
		
		public CompositeAttributeProvider(final ObjectName oname, final CompositeValueNavigator navigator, final Collection<? extends Object> attrs) throws IOException, OperationsException, ReflectionException{
			super(attrs != null ? attrs : new ArrayList<Object>());
			if(oname == null) throw new NullPointerException("oname is null");
			beanName = oname;
			this.navigator = navigator;
			//получить описатель поля, этот описатель может содержать знак @ для вложенного атрибута
			final MBeanAttributeInfo targetAttr = handleConnection(new DefaultServerConnectionHandler<MBeanAttributeInfo>() {
				
				public MBeanAttributeInfo handle(MBeanServerConnection connection) throws IOException, JMException {
					for(final MBeanAttributeInfo attr: connection.getMBeanInfo(oname).getAttributes())
						if(navigator.attributeName.equals(attr.getName())) return attr;
					return null;
				}
			});
			if(targetAttr == null) throw new AttributeNotFoundException(String.format("JMX attribute %s doesn't exist", navigator.attributeName));
			else this.compositeType = targetAttr instanceof OpenMBeanAttributeInfoSupport ? ((OpenMBeanAttributeInfoSupport)targetAttr).getOpenType() : SimpleType.STRING;
		}
		
		/**
		 * Получить текстовое название типа атрибута.
		 * @return
		 */
		
		public String getAttributeClassName()
		{
			return navigator.getType(compositeType);
		}
		
		/**
		 * Получить значение JMX-атрибута.
		 * @return Значение атрибута.
		 * @throws OperationsException
		 * @throws JMException
		 * @throws OperationsException
		 */
		
		public Object getValue(final Object defaultValue){
			return handleConnection(new MBeanServerConnectionHandler<Object>(){
				
				public Object handle(final MBeanServerConnection connection) throws IOException, JMException {
					return navigator.getValue(connection.getAttribute(beanName, navigator.attributeName));
				}

				
				public Object getDefaultValue(Exception e) {
					return defaultValue;
				}
			});
		}

		
		public ObjectName getOwner() {
			return beanName;
		}
	}

	public JmxMonitor(JMXServiceURL url, Map<String, ?> parameters) {
		if(url == null) throw new NullPointerException("url is null");
		if(parameters == null) throw new NullPointerException("parameters is null");
		_attributes = new HashMap<String, AttributeProvider>();
		_url = url;
		_connectionParameters = Collections.unmodifiableMap(parameters);
	}

	public JmxMonitor(final JMXServiceURL url, final String login,
			final String password) {
		this(url, new HashMap<String, String[]>(1) {
			private static final long serialVersionUID = -2609477306585254684L;
			{
				put(JMXConnector.CREDENTIALS, new String[] { login, password });
			}
		});
	}

	public JmxMonitor(final String RMIServerAddress, final String RMIServerPort, 
			final String RMIRegistryAddress, final String RMIRegistryPort,
			final String login, final String password) throws IOException,
			MalformedObjectNameException {
		this(new JMXServiceURL(String.format(connectionFormat, RMIServerAddress, RMIServerPort, RMIRegistryAddress, RMIRegistryPort)),
				login, password);
	}
	
	/**
	 * Произвести безопасные действия над JMX-соединением, поскольку данный метод сам обеспечивает
	 * логику синхронизации и переподключения.
	 * @param connectionHandler Блок кода, работающий с подключений, которому необходимо обеспечить синхронизация и переподключение.
	 * @return Результат работы с JMX-подключением
	 */
	protected synchronized <TOutput> TOutput handleConnection(MBeanServerConnectionHandler<TOutput> connectionHandler)
	{
		try{
			final JMXConnector connector = JMXConnectorFactory.connect(_url, _connectionParameters);
			final TOutput result = connectionHandler.handle(connector.getMBeanServerConnection());
			connector.close();
			return result;
		}
		catch(Exception e){
			return connectionHandler.getDefaultValue(e);
		}
	}
	
	public AttributeProvider addAttribute(ObjectName beanName, String beanAttribute, String attributeId, Set<Object> tags, boolean useRegExp){
		if(_attributes.containsKey(attributeId)) return null;
		final AttributeProvider provider;
		try {
			if (useRegExp)
				provider = new RegExAttributeProvider(beanName, beanAttribute, tags);
			else if(CompositeValueNavigator.isCompositeAttribute(beanAttribute))
				provider = new CompositeAttributeProvider(beanName, new CompositeValueNavigator(beanAttribute), tags);
			else 
				provider = new PlainAttributeProvider(beanName, beanAttribute, tags);
		} catch (OperationsException e) {
			System.err.println(String.format("Attribute %s of bean %s cannot be used. Reason: %s", beanAttribute, beanName, e));
			return null;
		} catch (ReflectionException e) {
			System.err.println(String.format("Attribute %s of bean %s cannot be used. Reason: %s", beanAttribute, beanName, e));
			return null;
		} catch (IOException e) {
			System.err.println(String.format("Attribute %s of bean %s cannot be used. Reason: %s", beanAttribute, beanName, e));
			return null;
		}
		_attributes.put(attributeId, provider);
		return provider;
	}
	
	public AttributeProvider addAttribute(String beanName, String beanAttribute, String attributeId, Set<Object> tags, boolean useRegExp){
		try {
			return addAttribute(new ObjectName(beanName), beanAttribute, attributeId, tags, useRegExp);
		} catch (MalformedObjectNameException e) {
			System.err.println(String.format("Атрибут %s бина %s не может быть использован. Информация: %s", beanAttribute, beanName, e));
			return null;
		}
	}
	
	public AttributeProvider addAttribute(String beanName, String beanAttribute, String attributeId, boolean useRegExp){
		return addAttribute(beanName, beanAttribute, attributeId, new HashSet<Object>(), useRegExp);
	}
	
	public AttributeProvider getAttribute(String attributeId){
		return _attributes.get(attributeId);
	}

	
	public Iterator<String> iterator() {
		return _attributes.keySet().iterator();
	}

}
