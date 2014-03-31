package com.snamp.configuration;

import com.snamp.TimeSpan;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents configuration file parser.
 * <p>
 *     This enum is an entry point for organizing agent configuration persistence.
 *     The following example demonstrates how to invoke agent configuration stored in YAML file:<br/>
 *     <pre>{@code
 *     final AgentConfiguration config = ConfigurationFormat.load("yaml", "~/docs/snamp.yaml");
 *     }</pre><br/>
 *     The current version of SNAMP supports the two persistent configuration formats:
 *     <ul>
 *         <li>YAML markup.</li>
 *         <li>Binary file (serialization/deserialization of {@link EmbeddedAgentConfiguration}.</li>
 *     </ul>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public enum ConfigurationFormat {

    /**
     * Represents YAML-formatted SNAMP agent configuration file.
     */
    YAML("yaml"),

    /**
     * Represents binary agent configuration file.
     */
    BINARY("bin");

    private final String _formatName;

    private ConfigurationFormat(String formatName) {
        _formatName = formatName;
    }

    /**
     * Returns MIME type for this SNAMP configuration format.
     * @return MIME type for this SNAMP configuration format.
     */
    public final String getMimeType(){
        switch (_formatName){
            case "yaml": return "application/x-yaml";
            case "bin": return "application/x-java-serialized-object";
            default: return "application/octet-stream";
        }
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
        private final static String managementTargetsKey = "managementTargets";
        private final static String connectionStringtKey = "connectionString";
        private final static String connectionTypetKey = "connectionType";
        private final static String namespaceKey = "namespace";
        private final static String defaultTimeoutKey = "defaultTimeout";
        private final static String attributesKey = "attributes";
        private final static String targetKey = "target";
        private final static String idKey = "id";
        private final static String readWriteTimeoutKey = "readWriteTimeout";
        private final static String nameKey = "name";
        private final static String hostingConfigurationKey = "adapter";
        private final static String adapterNameKey = "name";
        private final static String eventsKey = "events";
        private final static String categoryKey = "category";

        /**
         * Initializes a new empty configuration.
         */
        public YamlAgentConfiguration(){
            super(10);
        }

        private YamlAgentConfiguration(final Map<String, Object> dom){
            super(dom);
        }

        @Override
        public YamlAgentConfiguration clone() {
            return new YamlAgentConfiguration(this);
        }

        /**
         *
         * @param key
         * @param configuration
         * @return
         */
        private static Map<String,Object> convertAttributeToMap(final String key, final ManagementTargetConfiguration.AttributeConfiguration configuration){
            final Map<String,Object> tmpMap = new HashMap<>();
            if(configuration instanceof YamlAttributeConfiguration)
            {
                tmpMap.put(idKey, key);
                tmpMap.putAll((YamlAttributeConfiguration)configuration);
            }
            return tmpMap;
        }

        private static Map<String,Object> convertEventToMap(final String key, final ManagementTargetConfiguration.EventConfiguration configuration){
            final Map<String,Object> tmpMap = new HashMap<>();
            if(configuration instanceof YamlEventConfiguration)
            {
                tmpMap.put(idKey, key);
                tmpMap.putAll((YamlEventConfiguration)configuration);
            }
            return tmpMap;
        }

        /**Helper method to compose map from ManagementTargetConfiguration impl.*/
        private static Map<String,Object> convertConfigurationToMap(final String key, final AgentConfiguration.ManagementTargetConfiguration configuration)
        {
            final Map<String,Object> tmpMap = new HashMap<>();
            if(configuration instanceof YamlManagementTargetConfiguration)
            {
                tmpMap.put(targetKey, key);
                tmpMap.putAll((YamlManagementTargetConfiguration)configuration);
            }
            return tmpMap;
        }

        private static final class YamlManagementTargets implements Map<String, AgentConfiguration.ManagementTargetConfiguration>{
            private final List<Object> targets;

            public YamlManagementTargets(final List<Object> targets){
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
                throw new UnsupportedOperationException();
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
                final YamlManagementTargetConfiguration target = new YamlManagementTargetConfiguration();
                target.putAll(getValueByKey(key));
                return target;
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
                final Set<String> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {   final Object obj = targets.get(i);
                    if(obj instanceof Map)
                        tmpSet.add(Objects.toString(((Map<String,Object>)obj).get(targetKey)));
                }
                return tmpSet;
            }

            @Override
            public Collection<AgentConfiguration.ManagementTargetConfiguration> values() {
                final Collection<AgentConfiguration.ManagementTargetConfiguration> tmpCollection = new ArrayList<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        final YamlManagementTargetConfiguration target = new YamlManagementTargetConfiguration();
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
                                YamlManagementTargetConfiguration target = new YamlManagementTargetConfiguration();
                                target.putAll((Map<String,Object>)obj);
                                return target;
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

        private final static class YamlEvents implements Map<String, ManagementTargetConfiguration.EventConfiguration>{
            private final List<Object> targets;

            public YamlEvents(final List<Object> targets){
                this.targets = targets;
            }

            @Override
            public final int size() {
                return targets.size();
            }

            @Override
            public final boolean isEmpty() {
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
                throw new UnsupportedOperationException();
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
            public ManagementTargetConfiguration.EventConfiguration get(final Object key) {
                final YamlEventConfiguration attrs = new YamlEventConfiguration();
                attrs.putAll(getValueByKey(key));
                return attrs;
            }

            @Override
            public ManagementTargetConfiguration.EventConfiguration put(final String key, final ManagementTargetConfiguration.EventConfiguration value) {
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
                            targets.add(convertEventToMap(key, value));
                            found = true;
                            break;
                        }
                    }
                }
                if(!found)
                    targets.add(convertEventToMap(key, value));
                return null;
            }

            @Override
            public ManagementTargetConfiguration.EventConfiguration remove(final Object key) {
                final ManagementTargetConfiguration.EventConfiguration tmpConfig = get(key);
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
            public void putAll(Map<? extends String, ? extends ManagementTargetConfiguration.EventConfiguration> m) {
                for(Map.Entry<? extends String, ? extends ManagementTargetConfiguration.EventConfiguration> entry: m.entrySet())
                {
                    targets.add(convertEventToMap(entry.getKey(), entry.getValue()));
                }
            }

            @Override
            public void clear() {
                targets.clear();
            }

            @Override
            public Set<String> keySet() {
                final Set<String> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                        tmpSet.add(Objects.toString(((Map<String,Object>)obj).get(idKey)));
                }
                return tmpSet;
            }

            @Override
            public Collection<ManagementTargetConfiguration.EventConfiguration> values() {
                final Collection<ManagementTargetConfiguration.EventConfiguration> tmpCollection = new ArrayList<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        final YamlEventConfiguration attrs = new YamlEventConfiguration();
                        attrs.putAll((Map<String,Object>)obj);
                        tmpCollection.add(attrs);
                    }
                }
                return tmpCollection;
            }

            @Override
            public Set<Entry<String, ManagementTargetConfiguration.EventConfiguration>> entrySet() {
                final Set<Entry<String, ManagementTargetConfiguration.EventConfiguration>> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                        tmpSet.add(new Entry<String, ManagementTargetConfiguration.EventConfiguration>() {
                            @Override
                            public String getKey() {
                                return Objects.toString(((Map<String,Object>)obj).get(idKey), "");
                            }

                            @Override
                            public ManagementTargetConfiguration.EventConfiguration getValue() {
                                final YamlEventConfiguration attrs = new YamlEventConfiguration();
                                attrs.putAll((Map<String,Object>)obj);
                                return attrs;
                            }

                            @Override
                            public ManagementTargetConfiguration.EventConfiguration setValue(final ManagementTargetConfiguration.EventConfiguration value) {
                                throw new UnsupportedOperationException();
                            }
                        });
                }
                return tmpSet;
            }

        }

        private final static class YamlAttributes implements Map<String, ManagementTargetConfiguration.AttributeConfiguration>{
            private final List<Object> targets;
            private Long defaultTimeOut;

            public YamlAttributes(final List<Object> targets, final Long defaultTimeOut){
                this.targets = targets;
                this.defaultTimeOut = defaultTimeOut;
            }

            public YamlAttributes(final List<Object> targets){
                this(targets, 0L);
            }

            @Override
            public final int size() {
                return targets.size();
            }

            @Override
            public final boolean isEmpty() {
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
                throw new UnsupportedOperationException();
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
                final YamlAttributeConfiguration attrs = new YamlAttributeConfiguration(defaultTimeOut);
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
                final Set<String> tmpSet = new HashSet<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                        tmpSet.add(Objects.toString(((Map<String,Object>)obj).get(idKey)));
                }
                return tmpSet;
            }

            @Override
            public Collection<ManagementTargetConfiguration.AttributeConfiguration> values() {
                final Collection<ManagementTargetConfiguration.AttributeConfiguration> tmpCollection = new ArrayList<>();
                for(int i=0;i<targets.size();i++)
                {
                    final Object obj = targets.get(i);
                    if(obj instanceof Map)
                    {
                        final YamlAttributeConfiguration attrs = new YamlAttributeConfiguration(defaultTimeOut);
                        attrs.putAll((Map<String,Object>)obj);
                        tmpCollection.add(attrs);
                    }
                }
                return tmpCollection;
            }

            @Override
            public Set<Entry<String, ManagementTargetConfiguration.AttributeConfiguration>> entrySet() {
                final Set<Entry<String, ManagementTargetConfiguration.AttributeConfiguration>> tmpSet = new HashSet<>();
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
                                final YamlAttributeConfiguration attrs = new YamlAttributeConfiguration(defaultTimeOut);
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
            return new YamlManagementTargets((List<Object>)obj);
        }

        private final static class YamlEventConfiguration extends HashMap<String, Object> implements ManagementTargetConfiguration.EventConfiguration{

            public YamlEventConfiguration(){
                super(10);
            }

            /**
             * Gets the event category.
             *
             * @return The event category.
             */
            @Override
            public final String getCategory() {
                return Objects.toString(get(categoryKey), "");
            }

            /**
             * Sets the category of the event to listen.
             *
             * @param eventCategory The category of the event to listen.
             */
            @Override
            public final void setCategory(final String eventCategory) {
                put(categoryKey, eventCategory != null ? eventCategory : "");
            }

            /**
             * Gets a map of event options.
             *
             * @return The map of event options.
             */
            @Override
            public final Map<String, String> getAdditionalElements() {
                return new YamlAdditionalElementsMap(this, categoryKey, idKey);
            }
        }

        private final static class YamlAttributeConfiguration extends HashMap<String, Object> implements ManagementTargetConfiguration.AttributeConfiguration
        {
            public YamlAttributeConfiguration()
            {
                super();
            }

            public YamlAttributeConfiguration(final Long defaultTimeout)
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

        private static final class YamlManagementTargetConfiguration extends HashMap<String, Object> implements ManagementTargetConfiguration
        {
            public YamlManagementTargetConfiguration()
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
                    this.put(attributesKey, obj = new ArrayList<>());
                return new YamlAttributes((List<Object>)obj, Long.parseLong(Objects.toString(this.get(defaultTimeoutKey), "0")));
            }

            /**
             * Returns the event sources.
             *
             * @return A set of event sources.
             */
            @Override
            public final Map<String, EventConfiguration> getEvents() {
                Object obj = this.get(eventsKey);
                if(!(obj instanceof List))
                    this.put(eventsKey, obj = new ArrayList<>());
                return new YamlEvents((List<Object>)obj);
            }

            @Override
            public Map<String, String> getAdditionalElements() {
                return new YamlAdditionalElementsMap(this, connectionStringtKey, connectionTypetKey, namespaceKey, targetKey, attributesKey);
            }

            @Override
            public AttributeConfiguration newAttributeConfiguration() {
                return new YamlAttributeConfiguration();
            }

            /**
             * Creates an empty event configuration.
             * <p>
             * Usually, this method is used for adding new events in the collection
             * returned by {@link #getEvents()} method.
             * </p>
             *
             * @return An empty event configuration.
             */
            @Override
            public EventConfiguration newEventConfiguration() {
                return new YamlEventConfiguration();
            }
        }

        @Override
        public ManagementTargetConfiguration newManagementTargetConfiguration() {
            return new YamlManagementTargetConfiguration();
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
            AbstractAgentConfiguration.copy(input, this);
        }
    }

    /**
     * Returns the configuration file format parser from the format name.
     * @param format The configuration file format name.
     * @return An instance of the configuration file.
     */
    public static ConfigurationFormat parse(final String format) {
        switch (format){
            default:
            case "yaml": return YAML;
            case "bin": return BINARY;
        }
    }

    /**
     * Creates a new empty configuration of the specified format.
     * @param format The name of the configuration persistence format.
     * @return A new empty configuration that can be stored and restored to/from stream in
     * the specified format.
     */
    public static final AgentConfiguration newAgentConfiguration(final String format){
        return parse(format).newAgentConfiguration();
    }

    /**
     * Creates a new empty configuration of this format.
     * @return A new empty configuration of this format.
     */
    public final AgentConfiguration newAgentConfiguration(){
        switch (_formatName){
            case "yaml": return new YamlAgentConfiguration();
            case "bin": return new EmbeddedAgentConfiguration();
            default: return null;
        }
    }

    /**
     * Loads the configuration from the specified file.
     * @param format The configuration file format name.
     * @param fileName The path to the configuration file.
     * @return The parsed configuration.
     * @throws java.io.IOException Unable to load the SNAMP configuration from the specified file.
     */
    public static AgentConfiguration load(final String format, final String fileName) throws IOException {
        final AgentConfiguration config = newAgentConfiguration(format);
        try(final InputStream stream = new FileInputStream(fileName)){
            config.load(stream);
        }
        return config;
    }
}
