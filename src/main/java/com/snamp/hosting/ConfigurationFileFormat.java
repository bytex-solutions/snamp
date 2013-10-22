package com.snamp.hosting;

import com.snamp.TimeSpan;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
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
        private final static String hostingConfigurationKey = "adapter";
        private final static String adapterNameKey = "name";
        /**
         * Initializes a new empty configuration.
         */
        public YamlAgentConfiguration(){
            super(10);
        }

        /**
         *
         * @param key
         * @param configuration
         * @return
         */
        private static Map<String,Object> convertAttributeToMap(final String key, final ManagementTargetConfiguration.AttributeConfiguration configuration){
            final Map<String,Object> tmpMap = new HashMap<>();
            tmpMap.put(idKey, key);
            tmpMap.putAll((AttributeConfigurationImpl)configuration);
            return tmpMap;
        }

        /**Helper method to compose map from ManagementTargetConfiguration impl.*/
        private static Map<String,Object> convertConfigurationToMap(final String key, final AgentConfiguration.ManagementTargetConfiguration configuration)
        {
            final Map<String,Object> tmpMap = new HashMap<>();
            tmpMap.put(targetKey, key);
            tmpMap.putAll((ManagementTargetConfigurationImpl)configuration);
            return tmpMap;
        }

        private static final class YamlManagementTargetConfiguration implements Map<String, AgentConfiguration.ManagementTargetConfiguration>{
            private final List<Object> targets;

            public YamlManagementTargetConfiguration(final List<Object> targets){
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
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map && containsTarget(targetName, (Map<String,Object>)obj)) return true;
                }
                return false;
            }

            @Override
            public boolean containsValue(final Object value) {
                return false;
            }

            private Map<String, Object> getValueByKey(final Object key){
                for(int i = 0; i < targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {   final Object item = ((Map<String,Object>)obj).get(targetKey);
                        if(item != null && item.equals(Objects.toString(key, "")))
                            return (Map<String, Object>)obj;
                    }
                }
                return null;
            }

            @Override
            public AgentConfiguration.ManagementTargetConfiguration get(Object key) {
                ManagementTargetConfigurationImpl target = new ManagementTargetConfigurationImpl();
                target.putAll(getValueByKey(key));
                return target;//new ManagementTargetConfigurationImpl(getValueByKey(key));
            }

            @Override
            public AgentConfiguration.ManagementTargetConfiguration put(String key, AgentConfiguration.ManagementTargetConfiguration value) {
                boolean found = false;
                //If map is already has that key, we should change only value
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj =  targets.get(i);
                    if(obj instanceof Map)
                    {
                        final Object item = ((Map<String,Object>)obj).get(targetKey);
                        if(item != null && item.equals(key))
                        {
                            targets.remove(i);
                            targets.add(convertConfigurationToMap(key, value));
                            found = true;
                            break;
                        }
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
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        final Object item = ((Map<String,Object>)obj).get(targetKey);
                        if(item != null && item.equals(Objects.toString(key, "")))
                        {
                            targets.remove(i);
                            break;
                        }
                    }
                }
                return tmpConfig;
            }

            @Override
            public final void putAll(Map<? extends String, ? extends AgentConfiguration.ManagementTargetConfiguration> m) {
                for(Map.Entry<? extends String, ? extends AgentConfiguration.ManagementTargetConfiguration> entry: m.entrySet())
                    targets.add(convertConfigurationToMap(entry.getKey(), entry.getValue()));
            }

            @Override
            public final void clear() {
                targets.clear();
            }

            @Override
            public Set<String> keySet() {
                Set<String> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {   final Object obj = targets.get(i);
                    if(obj instanceof Map)
                        tmpSet.add(Objects.toString(((Map<String,Object>)obj).get(targetKey)));
                }
                return tmpSet;
            }

            @Override
            public Collection<AgentConfiguration.ManagementTargetConfiguration> values() {
                Collection<AgentConfiguration.ManagementTargetConfiguration> tmpCollection = new ArrayList<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        //tmpCollection.add(new ManagementTargetConfigurationImpl((Map<String,Object>)obj));
                        final ManagementTargetConfigurationImpl target = new ManagementTargetConfigurationImpl();
                        target.putAll((Map<String,Object>)obj);
                        tmpCollection.add(target);
                    }
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
                            final Object obj = targets.get(j);
                            if(obj instanceof Map)
                                return Objects.toString(((Map<String,Object>)obj).get(targetKey));
                            else
                                return null;
                        }

                        @Override
                        public AgentConfiguration.ManagementTargetConfiguration getValue() {
                            final Object obj = targets.get(j);
                            if(obj instanceof Map)
                            {
                                ManagementTargetConfigurationImpl target = new ManagementTargetConfigurationImpl();
                                target.putAll((Map<String,Object>)obj);
                                return target;//new ManagementTargetConfigurationImpl((Map<String,Object>)obj);
                            }
                            else
                                return null;
                        }

                        @Override
                        public AgentConfiguration.ManagementTargetConfiguration setValue(AgentConfiguration.ManagementTargetConfiguration value) {
                            return null;
                        }
                    });
                }
                return tmpSet;
            }

        }

        private final static class YamlAttributeConfiguration implements Map<String, ManagementTargetConfiguration.AttributeConfiguration>{
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
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map && containsTarget(targetName, (Map<String,Object>)obj)) return true;
                }
                return false;
            }

            @Override
            public boolean containsValue(final Object value) {
                return false;
            }

            private Map<String, Object> getValueByKey(final Object key){
                for(int i = 0; i < targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        final Object item = ((Map<String,Object>)obj).get(idKey);
                        if(item != null && item.equals(Objects.toString(key, "")))
                            return (Map<String, Object>)obj;
                    }
                }
                return null;
            }

            @Override
            public ManagementTargetConfiguration.AttributeConfiguration get(Object key) {
                final AttributeConfigurationImpl attrs = new AttributeConfigurationImpl(defaultTimeOut);
                attrs.putAll(getValueByKey(key));
                return attrs;
            }

            @Override
            public ManagementTargetConfiguration.AttributeConfiguration put(String key, ManagementTargetConfiguration.AttributeConfiguration value) {
                boolean found = false;
                //If map is already has that key, we should change only value
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        final Object item = ((Map<String,Object>)obj).get(idKey);
                        if(item != null && item.equals(Objects.toString(key, "")))
                        {
                            targets.remove(i);
                            targets.add(convertAttributeToMap(key, value));
                            found = true;
                            break;
                        }
                    }
                }
                if(!found)
                    targets.add(convertAttributeToMap(key, value));
                return null;
            }

            @Override
            public ManagementTargetConfiguration.AttributeConfiguration remove(Object key) {
                final ManagementTargetConfiguration.AttributeConfiguration tmpConfig = get(key);
                for(int i = 0; i < targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        final Object item = ((Map<String,Object>)obj).get(idKey);
                        if(item != null && item.equals(Objects.toString(key, "")))
                        {
                            targets.remove(i);
                            break;
                        }
                    }
                }
                return tmpConfig;
            }

            @Override
            public void putAll(Map<? extends String, ? extends ManagementTargetConfiguration.AttributeConfiguration> m) {
                for(Map.Entry<? extends String, ? extends ManagementTargetConfiguration.AttributeConfiguration> entry: m.entrySet())
                {
                    targets.add(convertAttributeToMap(entry.getKey(), entry.getValue()));
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
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                        tmpSet.add(Objects.toString(((Map<String,Object>)obj).get(idKey)));
                }
                return tmpSet;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Collection<ManagementTargetConfiguration.AttributeConfiguration> values() {
                Collection<ManagementTargetConfiguration.AttributeConfiguration> tmpCollection = new ArrayList<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        //tmpCollection.add(new AttributeConfigurationImpl((Map<String,Object>)obj, defaultTimeOut));
                        final AttributeConfigurationImpl attrs = new AttributeConfigurationImpl(defaultTimeOut);
                        attrs.putAll((Map<String,Object>)obj);
                        tmpCollection.add(attrs);
                    }
                }
                return tmpCollection;
            }

            @Override
            public Set<Entry<String, ManagementTargetConfiguration.AttributeConfiguration>> entrySet() {
                Set<Entry<String, ManagementTargetConfiguration.AttributeConfiguration>> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                        tmpSet.add(new Entry<String, ManagementTargetConfiguration.AttributeConfiguration>() {
                            @Override
                            public String getKey() {
                                return Objects.toString(((Map<String,Object>)obj).get(idKey), "");
                            }

                            @Override
                            public ManagementTargetConfiguration.AttributeConfiguration getValue() {
                                final AttributeConfigurationImpl attrs = new AttributeConfigurationImpl(defaultTimeOut);
                                attrs.putAll((Map<String,Object>)obj);
                                return attrs;
                            }

                            @Override
                            public ManagementTargetConfiguration.AttributeConfiguration setValue(ManagementTargetConfiguration.AttributeConfiguration value) {
                                return null;
                            }
                        });
                }
                return tmpSet;
            }

        }
        private final static class YamlAdditionalElementsMap implements Map<String, String> {
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
                return internalSet;
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
            Object obj =  this.get(managementTargetsKey);
            if(!(obj instanceof List))
                this.put(managementTargetsKey, obj = new ArrayList<>());
            return new YamlManagementTargetConfiguration((List<Object>)obj);
        }

        private final static class AttributeConfigurationImpl extends HashMap<String, Object> implements ManagementTargetConfiguration.AttributeConfiguration
        {
            public AttributeConfigurationImpl()
            {
                super();
            }

            public AttributeConfigurationImpl(final Long defaultTimeout)
            {
                super();
                if(defaultTimeout > 0)
                    this.put(readWriteTimeoutKey, defaultTimeout);

            }
            @Override
            public TimeSpan getReadWriteTimeout() {
                return new TimeSpan(Long.parseLong(Objects.toString(this.get(readWriteTimeoutKey), "0")));
            }

            @Override
            public void setReadWriteTimeout(TimeSpan time) {
                this.put(readWriteTimeoutKey, time.convert(TimeUnit.MILLISECONDS).duration);
            }

            @Override
            public String getAttributeName() {
                return Objects.toString(this.get(nameKey), "");
            }

            @Override
            public void setAttributeName(String attributeName) {
                this.put(nameKey, attributeName);
            }

            @Override
            public Map<String, String> getAdditionalElements() {
                return new YamlAdditionalElementsMap(this, idKey, readWriteTimeoutKey, nameKey);
            }
        }

        private static final class ManagementTargetConfigurationImpl extends HashMap<String, Object> implements ManagementTargetConfiguration
        {
            public ManagementTargetConfigurationImpl()
            {
                super();
            }

            @Override
            public String getConnectionString() {
                return Objects.toString(this.get(connectionStringtKey), "");
            }

            @Override
            public void setConnectionString(String connectionString) {
                this.put(connectionStringtKey, connectionString);
            }

            @Override
            public String getConnectionType() {
                return Objects.toString(this.get(connectionTypetKey), "");
            }

            @Override
            public void setConnectionType(String connectorType) {
                this.put(connectionTypetKey, connectorType);
            }

            @Override
            public String getNamespace() {
                return Objects.toString(this.get(namespaceKey), "");
            }

            @Override
            public void setNamespace(String namespace) {
                this.put(namespaceKey, namespace);
            }

            @Override
            public Map<String, AttributeConfiguration> getAttributes() {
                Object obj = this.get(attributesKey);
                if(!(obj instanceof List))
                {
                    this.put(attributesKey, obj = new ArrayList<>());
                }
                return new YamlAttributeConfiguration((List<Object>)obj, Long.parseLong(Objects.toString(this.get(defaultTimeoutKey), "0")));
            }

            @Override
            public Map<String, String> getAdditionalElements() {
                return new YamlAdditionalElementsMap(this, connectionStringtKey, connectionTypetKey, namespaceKey, targetKey, attributesKey);
            }

            @Override
            public AttributeConfiguration newAttributeConfiguration() {
                return new AttributeConfigurationImpl();
            }
        }
        @Override
        public ManagementTargetConfiguration newManagementTargetConfiguration() {
            return new ManagementTargetConfigurationImpl();
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

        /**
         * Imports the state of specified object into this object.
         *
         * @param input
         */
        @Override
        public final void load(final AgentConfiguration input) {
            AgentConfigurationBase.copy(input, this);
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
    public static final AgentConfiguration newAgentConfiguration(final String format){
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
        catch(final FileNotFoundException ignored) {
            System.out.println("No input configuration file specified!");
            System.exit(0);
        }
        return config;
    }
}
