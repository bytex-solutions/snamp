package com.snamp.hosting;

import com.snamp.TimeSpan;
import com.snamp.connectors.ManagementConnector;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents configuration file parser.
 */
public enum ConfigurationFileFormat{

    /**
	 * Формат файла YAML
	 */
	YAML("yaml");

	private final String _formatName;
	
	private ConfigurationFileFormat(String formatName) {
		_formatName = formatName;
	}
	
	/**
	 * Returns a string representation of this file format.
     * @return The name of the format.
	 */
	@Override
	public final String toString() {
		return _formatName;
	}

    /**
     * Represents YAML-compliant configuration.
     */
    private static final class YamlAgentConfiguration extends HashMap<String, Object> implements AgentConfiguration{
        private static final String managementTargetsKey = "managementTargets";
        private final static String connectionStringtKey = "connectionString";
        private final static String connectionTypetKey = "connectionType";
        private final static String namespaceKey = "namespace";
        private final static String defaultTimeoutKey = "defaultTimeout";
        private final static String attributesKey = "attributes";
        private static final String targetKey = "target";
        private final static String idKey = "id";
        private final static String readWriteTimeoutKey = "readWriteTimeout";
        private final static String nameKey = "name";
        private final static String hostingConfigurationKey = "hostingConfiguration";
        private final static String adapterNameKey = "adapterName";
        /**
         * Initializes a new empty configuration.
         */
        public YamlAgentConfiguration(){
            super(10);
        }

        private final class YamlManagementTargetConfigurations implements Map<String, AgentConfiguration.ManagementTargetConfiguration>{
            private final List<Object> targets;

            public YamlManagementTargetConfigurations(final List<Object> targets){
                this.targets = targets;
            }

            @Override
            public int size() {
                return targets.size();
            }

            @Override
            public boolean isEmpty() {
                return targets.isEmpty();
            }

            private boolean containsTarget(final Object targetName, Map<String, Object> targetConfig){
                return Objects.equals(targetConfig.get(targetKey), targetName);
            }

            @Override
            public boolean containsKey(final Object targetName) {
                for(int i=0;i<targets.size();i++)
                    if(containsTarget(targetName, (Map<String,Object>)targets.get(i))) return true;
                return false;
            }

            @Override
            public boolean containsValue(final Object value) {
            /*for(final Map<String, Object> targetEntry: targets)
                if(containsTarget(targetName, targetEntry)) return true;
            return false;*/
                return false;
            }

            private Map<String, Object> getValueByKey(final Object key){
                for(int i = 0; i < targets.size();i++)
                {
                    if(((Map<String,Object>)targets.get(i)).get(targetKey).equals(Objects.toString(key)))
                        return (Map<String, Object>)targets.get(i);
                }
                return null;
            }

            @Override
            public AgentConfiguration.ManagementTargetConfiguration get(Object key) {
                return new ManagementTargetConfigurationImpl(getValueByKey(key));
            }

            private Map<String,Object> convertAttributeToMap(final String key, final ManagementTargetConfiguration.AttributeConfiguration configuration)
            {   final Map<String,Object> tmpMap = new HashMap<>();
                //Convert ManagementTargetConfiguration to Map
                tmpMap.put(idKey, key);
                tmpMap.put(readWriteTimeoutKey, configuration.getReadWriteTimeout().duration);
                tmpMap.put(nameKey, configuration.getAttributeName());
                for(Map.Entry<String,String> entry:configuration.getAdditionalElements().entrySet())
                    tmpMap.put(entry.getKey(), entry.getValue());
                return tmpMap;
            }

            private Map<String,Object> convertConfigurationToMap(final String key, final AgentConfiguration.ManagementTargetConfiguration configuration)
            {   final Map<String,Object> tmpMap = new HashMap<>();
                //Convert ManagementTargetConfiguration to Map
                tmpMap.put(targetKey, key);
                tmpMap.put(connectionStringtKey, configuration.getConnectionString());
                tmpMap.put(connectionTypetKey, configuration.getConnectionType());
                tmpMap.put(namespaceKey, configuration.getNamespace());
                //
                List<Object> attributes = new ArrayList<>();
                for(Map.Entry<String, ManagementTargetConfiguration.AttributeConfiguration> entry: configuration.getAttributes().entrySet())
                {
                    attributes.add(convertAttributeToMap(entry.getKey(), entry.getValue()));
                }
                tmpMap.put(attributesKey, attributes);

                //
                for(Map.Entry<String,String> entry:configuration.getAdditionalElements().entrySet())
                    tmpMap.put(entry.getKey(), entry.getValue());
                return tmpMap;
            }
            @Override
            public AgentConfiguration.ManagementTargetConfiguration put(String key, AgentConfiguration.ManagementTargetConfiguration value) {
                boolean found = false;
                //If map is already has that key, we should change only value
                for(int i=0;i<targets.size();i++)
                {
                    if(((Map<String,Object>)targets.get(i)).get(targetKey).equals(key))
                    {
                        targets.remove(i);
                        targets.add(convertConfigurationToMap(key, value));
                        found = true;
                        break;
                    }
                }
                if(!found)
                    targets.add(convertConfigurationToMap(key, value));
                return null;
            }

            @Override
            public AgentConfiguration.ManagementTargetConfiguration remove(Object key) {
                final AgentConfiguration.ManagementTargetConfiguration tmpConfig = get(key);
                for(int i = 0; i < targets.size();i++)
                {
                    if(((Map<String,Object>)targets.get(i)).get(targetKey).equals(Objects.toString(key)))
                    {
                        targets.remove(i);
                        break;
                    }
                }
                return tmpConfig;
            }

            @Override
            public void putAll(Map<? extends String, ? extends AgentConfiguration.ManagementTargetConfiguration> m) {
                for(Map.Entry<? extends String, ? extends AgentConfiguration.ManagementTargetConfiguration> entry: m.entrySet())
                {
                    targets.add(convertConfigurationToMap(entry.getKey(), entry.getValue()));
                }
            }

            @Override
            public void clear() {
                targets.clear();
            }

            @Override
            public Set<String> keySet() {
                Set<String> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {
                    tmpSet.add(Objects.toString(((Map<String,Object>)targets).get(targetKey)));
                }
                return tmpSet;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Collection<AgentConfiguration.ManagementTargetConfiguration> values() {
                Collection<AgentConfiguration.ManagementTargetConfiguration> tmpCollection = new ArrayList<>();
                for(int i=0;i<targets.size();i++)
                {
                    tmpCollection.add(new ManagementTargetConfigurationImpl((Map<String,Object>)targets.get(i)));
                }
                return tmpCollection;
            }

            @Override
            public Set<Entry<String, AgentConfiguration.ManagementTargetConfiguration>> entrySet() {
                Set<Entry<String, AgentConfiguration.ManagementTargetConfiguration>> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {
                    final int j = i;
                    tmpSet.add(new Entry<String, AgentConfiguration.ManagementTargetConfiguration>() {
                        @Override
                        public String getKey() {
                            return Objects.toString(((Map<String,Object>)targets.get(j)).get(targetKey));
                        }

                        @Override
                        public AgentConfiguration.ManagementTargetConfiguration getValue() {
                            return new ManagementTargetConfigurationImpl((Map<String,Object>)targets.get(j));
                        }

                        @Override
                        public AgentConfiguration.ManagementTargetConfiguration setValue(AgentConfiguration.ManagementTargetConfiguration value) {
                            return null;
                        }
                    });
                }
                return tmpSet;  //To change body of implemented methods use File | Settings | File Templates.
            }

        }

        private class ManagementTargetConfigurationImpl implements AgentConfiguration.ManagementTargetConfiguration{

            private Map<String, Object> configMap;

            public ManagementTargetConfigurationImpl(Map<String, Object> configMap)
            {
                this.configMap = configMap;
            }

            @Override
            public String getConnectionString() {
                return Objects.toString(configMap.get(connectionStringtKey));
            }

            @Override
            public void setConnectionString(String connectionString) {
                configMap.put(connectionStringtKey, connectionString);
            }

            @Override
            public String getConnectionType() {
                return Objects.toString(configMap.get(connectionTypetKey));
            }

            @Override
            public void setConnectionType(String connectorType) {
                configMap.put(connectionTypetKey, connectorType);
            }

            @Override
            public String getNamespace() {
                return Objects.toString(configMap.get(namespaceKey));
            }

            @Override
            public void setNamespace(String namespace) {
                configMap.put(namespaceKey, namespace);
            }

            @Override
            public Map<String, AttributeConfiguration> getAttributes() {
                final List<Object> attributes = (List<Object>)configMap.get(attributesKey);
                if(attributes == null) return null;

                return new YamlAttributeConfiguration(attributes, Long.parseLong(Objects.toString(configMap.get(defaultTimeoutKey))));
            }

            @Override
            public Map<String, String> getAdditionalElements() {
                return new YamlAdditionalElementsMap(configMap, connectionStringtKey, connectionTypetKey, namespaceKey, defaultTimeoutKey);
            }

            @Override
            public AttributeConfiguration newAttributeConfiguration() {
                return new AttributeConfigurationEmptyImpl();
            }

            private final class AttributeConfigurationImpl implements AttributeConfiguration
            {
                private Map<String, Object> attrMap;
                private Long defaultTimeOut;

                public AttributeConfigurationImpl(final Map<String, Object> attrMap, final Long defaultTimeOut)
                {
                    this.attrMap = attrMap;
                    this.defaultTimeOut = defaultTimeOut;
                }

                public AttributeConfigurationImpl(final Map<String, Object> attrMap)
                {
                    this(attrMap, 0L);
                }

                @Override
                public TimeSpan getReadWriteTimeout() {
                    //Check if readWriteTimeout set up
                    Object tmpTimeout = attrMap.get(readWriteTimeoutKey);
                    if(tmpTimeout == null)
                    {
                        attrMap.put(readWriteTimeoutKey, defaultTimeOut);
                    }
                    return new TimeSpan(Long.parseLong(Objects.toString(attrMap.get(readWriteTimeoutKey))), TimeUnit.MILLISECONDS);
                }

                @Override
                public void setReadWriteTimeout(TimeSpan time) {
                    attrMap.put(readWriteTimeoutKey, time.convert(TimeUnit.MILLISECONDS).duration);
                }

                @Override
                public String getAttributeName() {
                    return Objects.toString(attrMap.get(nameKey));
                }

                @Override
                public void setAttributeName(String attributeName) {
                    attrMap.put(nameKey, attributeName);
                }

                @Override
                public Map<String, String> getAdditionalElements() {
                    return new YamlAdditionalElementsMap(attrMap, readWriteTimeoutKey, nameKey, defaultTimeoutKey);
                }
            }

            private final class YamlAttributeConfiguration implements Map<String, AttributeConfiguration>{
                private final List<Object> targets;
                private Long defaultTimeOut;

                public YamlAttributeConfiguration(final List<Object> targets, final Long defaultTimeOut){
                    this.targets = targets;
                    this.defaultTimeOut = defaultTimeOut;
                }

                public YamlAttributeConfiguration(final List<Object> targets){
                    this(targets, 0L);
                }

                @Override
                public int size() {
                    return targets.size();
                }

                @Override
                public boolean isEmpty() {
                    return targets.isEmpty();
                }

                private boolean containsTarget(final Object targetName, Map<String, Object> targetConfig){
                    return Objects.equals(targetConfig.get(idKey), targetName);
                }

                @Override
                public boolean containsKey(final Object targetName) {
                    for(int i=0;i<targets.size();i++)
                        if(containsTarget(targetName, (Map<String,Object>)targets.get(i))) return true;
                    return false;
                }

                @Override
                public boolean containsValue(final Object value) {
            /*for(final Map<String, Object> targetEntry: targets)
                if(containsTarget(targetName, targetEntry)) return true;
            return false;*/
                    return false;
                }

                private Map<String, Object> getValueByKey(final Object key){
                    for(int i = 0; i < targets.size();i++)
                    {
                        if(((Map<String,Object>)targets.get(i)).get(idKey).equals(Objects.toString(key)))
                            return (Map<String, Object>)targets.get(i);
                    }
                    return null;
                }

                @Override
                public AttributeConfiguration get(Object key) {
                    return new AttributeConfigurationImpl(getValueByKey(key), defaultTimeOut);
                }

                private Map<String,Object> convertConfigurationToMap(final String key, final AttributeConfiguration configuration)
                {   final Map<String,Object> tmpMap = new HashMap<>();
                    //Convert ManagementTargetConfiguration to Map
                    tmpMap.put(idKey, key);
                    tmpMap.put(readWriteTimeoutKey, configuration.getReadWriteTimeout().duration);
                    tmpMap.put(nameKey, configuration.getAttributeName());
                    for(Map.Entry<String,String> entry:configuration.getAdditionalElements().entrySet())
                        tmpMap.put(entry.getKey(), entry.getValue());
                    return tmpMap;
                }

                @Override
                public AttributeConfiguration put(String key, AttributeConfiguration value) {
                    boolean found = false;
                    //If map is already has that key, we should change only value
                    for(int i=0;i<targets.size();i++)
                    {
                        if(((Map<String,Object>)targets.get(i)).get(targetKey).equals(key))
                        {
                            targets.remove(i);
                            targets.add(convertConfigurationToMap(key, value));
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                        targets.add(convertConfigurationToMap(key, value));
                    return null;
                }

                @Override
                public AttributeConfiguration remove(Object key) {
                    final AttributeConfiguration tmpConfig = get(key);
                    for(int i = 0; i < targets.size();i++)
                    {
                        if(((Map<String,Object>)targets.get(i)).get(idKey).equals(Objects.toString(key)))
                        {
                            targets.remove(i);
                            break;
                        }
                    }
                    return tmpConfig;
                }

                @Override
                public void putAll(Map<? extends String, ? extends AttributeConfiguration> m) {
                    for(Map.Entry<? extends String, ? extends AttributeConfiguration> entry: m.entrySet())
                    {
                        targets.add(convertConfigurationToMap(entry.getKey(), entry.getValue()));
                    }
                }

                @Override
                public void clear() {
                    targets.clear();
                }

                @Override
                public Set<String> keySet() {
                    Set<String> tmpSet = new HashSet<>();
                    for(int i=0;i<targets.size();i++)
                    {
                        tmpSet.add(Objects.toString(((Map<String,Object>)targets).get(idKey)));
                    }
                    return tmpSet;  //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public Collection<AttributeConfiguration> values() {
                    Collection<AttributeConfiguration> tmpCollection = new ArrayList<>();
                    for(int i=0;i<targets.size();i++)
                    {
                        tmpCollection.add(new AttributeConfigurationImpl((Map<String,Object>)targets.get(i), defaultTimeOut));
                    }
                    return tmpCollection;
                }

                @Override
                public Set<Entry<String, AttributeConfiguration>> entrySet() {
                    Set<Entry<String, AttributeConfiguration>> tmpSet = new HashSet<>();
                    for(int i=0;i<targets.size();i++)
                    {
                        final int j = i;
                        tmpSet.add(new Entry<String, AttributeConfiguration>() {
                            @Override
                            public String getKey() {
                                return Objects.toString(((Map<String,Object>)targets.get(j)).get(idKey));
                            }

                            @Override
                            public AttributeConfiguration getValue() {
                                return new AttributeConfigurationImpl((Map<String,Object>)targets.get(j), defaultTimeOut);
                            }

                            @Override
                            public AttributeConfiguration setValue(AttributeConfiguration value) {
                                return null;
                            }
                        });
                    }
                    return tmpSet;
                }

            }
        }

        private final class YamlAdditionalElementsMap implements Map<String, String> {
            private Map<String,Object> internalMap;
            private String[] internalLegalKeys = new String[0];

            public YamlAdditionalElementsMap(final Map<String,Object> map, String ... legalKeys)
            {
                internalMap = map;
                if(legalKeys != null)
                    internalLegalKeys = legalKeys.clone();
            }
            private boolean isKeyLegal(final String key)
            {
                for(String legalKey: internalLegalKeys)
                    if(legalKey.equals(key))
                        return false;
                return true;
            }

            @Override
            public int size() {
                return internalMap.size()-internalLegalKeys.length;
            }

            @Override
            public boolean isEmpty() {
                return size() == 0? true: false;
            }

            @Override
            public boolean containsKey(Object key) {
                if(isKeyLegal(Objects.toString(key)))
                    return internalMap.containsKey(key);
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                for(Map.Entry<String, Object> entry : internalMap.entrySet())
                {
                    if(isKeyLegal(entry.getKey()))
                    {
                        if(entry.getValue().equals(value))
                            return true;
                    }
                }
                return false;
            }

            @Override
            public String get(Object key) {
                if(isKeyLegal(Objects.toString(key)))
                    return Objects.toString(internalMap.get(key));
                else
                    return null;
            }

            @Override
            public String put(String key, String value) {
                if(isKeyLegal(key))
                    return Objects.toString(internalMap.put(key, Objects.toString(value)), null);
                else
                    return null;
            }

            @Override
            public String remove(Object key) {
                if(isKeyLegal(Objects.toString(key)))
                    return Objects.toString(internalMap.remove(key), null);
                else
                    return null;
            }

            @Override
            public void putAll(Map<? extends String, ? extends String> m) {
                for(final String key: m.keySet())
                {
                    if(isKeyLegal(Objects.toString(key)))
                        internalMap.put(key, m.get(key));
                }
            }

            @Override
            public void clear() {
                for(Map.Entry<String, Object> entry : internalMap.entrySet())
                {
                    if(isKeyLegal(entry.getKey()))
                    {
                        internalMap.remove(entry.getKey());
                    }
                }
            }

            @Override
            public Set<String> keySet() {
                final Set<String> internalSet = new HashSet<String>();
                for(Map.Entry<String, Object> entry : internalMap.entrySet())
                {
                    if(isKeyLegal(entry.getKey()))
                    {
                        internalSet.add(entry.getKey());
                    }
                }
                return internalSet;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Collection<String> values() {
                final Collection<String> internalList = new ArrayList<String>();
                for(Map.Entry<String, Object> entry : internalMap.entrySet())
                {
                    if(isKeyLegal(entry.getKey()))
                    {
                        internalList.add(Objects.toString(entry.getValue(), ""));
                    }
                }
                return internalList;
            }

            @Override
            public Set<Entry<String, String>> entrySet() {
                final Set<Map.Entry<String, String>> internalSet = new HashSet<Map.Entry<String, String>>();
                for(final Map.Entry<String, Object> entry : internalMap.entrySet())
                {
                    if(isKeyLegal(entry.getKey()))
                    {
                        internalSet.add(new Entry<String, String>(){

                            @Override
                            public String getKey() {
                                return entry.getKey();
                            }

                            @Override
                            public String getValue() {
                                return Objects.toString(entry.getValue());
                            }

                            @Override
                            public String setValue(String value) {
                                return null;
                            }
                        });
                    }
                }
                return internalSet;
            }
        }

        /**
         * Returns the agent hosting configuration.
         *
         * @return The agent hosting configuration.
         */
        @Override
        public HostingConfiguration getAgentHostingConfig() {
            final Map<String, Object> conn = (Map<String, Object>)this.get(hostingConfigurationKey);
            return new AgentConfiguration.HostingConfiguration(){
                @Override
                public String getAdapterName() {
                    return Objects.toString(conn.get(adapterNameKey), "");
                }

                @Override
                public void setAdapterName(final String adapterName) {
                    conn.put(adapterNameKey, adapterName);
                }

                @Override
                public Map<String, String> getHostingParams() {
                    return new YamlAdditionalElementsMap(conn, adapterNameKey);
                }
            };
        }

        /**
         * Represents management targets.
         *
         * @return The dictionary of management targets (management back-ends).
         */
        @Override
        public Map<String, ManagementTargetConfiguration> getTargets() {
            final WeakReference<List<Object>> weakRefList = new WeakReference((List<Object>)this.get(managementTargetsKey));

            return new YamlManagementTargetConfigurations(weakRefList.get());
        }

        private class AttributeConfigurationEmptyImpl implements ManagementTargetConfiguration.AttributeConfiguration
        {
            private Long readWriteTimeout;
            private String attributeName;
            private Map<String, String> additionalElements;

            public AttributeConfigurationEmptyImpl()
            {
               this.readWriteTimeout = 0L;
               this.attributeName = "";
               this.additionalElements = new HashMap<>();
            }

            @Override
            public TimeSpan getReadWriteTimeout() {
                return new TimeSpan(this.readWriteTimeout);
            }

            @Override
            public void setReadWriteTimeout(TimeSpan time) {
                this.readWriteTimeout = time.convert(TimeUnit.MILLISECONDS).duration;
            }

            @Override
            public String getAttributeName() {
                return this.attributeName;
            }

            @Override
            public void setAttributeName(String attributeName) {
                this.attributeName = attributeName;
            }

            @Override
            public Map<String, String> getAdditionalElements() {
                return this.additionalElements;
            }
        }

        private class ManagementTargetConfigurationEmptyImpl implements ManagementTargetConfiguration
        {
            private String connectionString;
            private String connectorType;
            private String namespace;
            private Map<String, AttributeConfiguration> attributes;
            private Map<String, String> additionalElements;

            public ManagementTargetConfigurationEmptyImpl()
            {
                this.connectionString = "";
                this.connectorType = "";
                this.namespace = "";
                this.attributes = new HashMap<>();
                this.additionalElements = new HashMap<>();
            }

            @Override
            public String getConnectionString() {
                return this.connectionString;
            }

            @Override
            public void setConnectionString(String connectionString) {
                this.connectionString = connectionString;
            }

            @Override
            public String getConnectionType() {
                return this.connectorType;
            }

            @Override
            public void setConnectionType(String connectorType) {
                this.connectorType = connectorType;
            }

            @Override
            public String getNamespace() {
                return this.namespace;
            }

            @Override
            public void setNamespace(String namespace) {
                this.namespace = namespace;
            }

            @Override
            public Map<String, AttributeConfiguration> getAttributes() {
                return this.attributes;
            }

            @Override
            public Map<String, String> getAdditionalElements() {
                return this.additionalElements;
            }

            @Override
            public AttributeConfiguration newAttributeConfiguration() {
                return new AttributeConfigurationEmptyImpl();
            }
        }
        @Override
        public ManagementTargetConfiguration newManagementTargetConfiguration() {
            return new ManagementTargetConfigurationEmptyImpl();
        }

        /**
         * Saves the current configuration into the specified stream.
         *
         * @param output
         * @throws UnsupportedOperationException Serialization is not supported.
         */
        @Override
        public void save(final OutputStream output) throws IOException {
            final Yaml yaml = new Yaml();
            final String result = yaml.dumpAsMap(this);
            output.write(result.getBytes(Charset.forName("UTF-8")));
        }

        /**
         * Reads the file and fills the current instance.
         *
         * @param input
         */
        @Override
        public void load(final InputStream input) {
            final Yaml yaml = new Yaml();
            final Map<String, Object> dom = (Map<String, Object>)yaml.load(input);
            clear();
            putAll(dom);
        }
    }

    /**
     * Returns the configuration file format parser from the format name.
     * @param format The configuration file format name.
     * @return An instance of the configuration file.
     */
	public static ConfigurationFileFormat parse(final String format) {
		switch (format){
            default:
            case "yaml": return YAML;
        }
	}

    /**
     * Creates a new empty configuration of the specified format.
     * @param format
     * @return
     */
    public static AgentConfiguration newAgentConfiguration(final String format){
        return parse(format).newAgentConfiguration();
    }

    /**
     * Creates a new empty configuration of this format.
     * @return
     */
    public final AgentConfiguration newAgentConfiguration(){
        switch (_formatName){
            case "yaml": return new YamlAgentConfiguration();
            default: return null;
        }
    }

    /**
     * Loads the configuration from the specified file.
     * @param format The configuration file format name.
     * @param fileName The path to the configuration file.
     * @return The parsed configuration.
     */
    public static AgentConfiguration load(final String format, final String fileName) throws IOException {
        final AgentConfiguration config = newAgentConfiguration(format);
        try(final InputStream stream = new FileInputStream(fileName)){
            config.load(stream);
        }
        return config;
    }
}
