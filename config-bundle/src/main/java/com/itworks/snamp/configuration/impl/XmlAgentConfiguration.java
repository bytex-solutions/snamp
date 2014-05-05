package com.itworks.snamp.configuration.impl;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AbstractAgentConfiguration;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;

/**
 * Represents JAXB-compliant representation of the SNAMP configuration. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(namespace = XmlConstants.namespace, name = "snamp")
@XmlType(namespace = XmlConstants.namespace, name = "SnampConfig")
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class XmlAgentConfiguration extends AbstractAgentConfiguration {
    /**
     * Represents XML map entry. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @XmlType(name = "ConfigurationParameter", namespace = XmlConstants.namespace)
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public final static class XmlMapEntry {
        @XmlTransient
        private String key;
        @XmlTransient
        private String value;

        /**
         * Initializes a new map entry with the specified key and value.
         * @param k The key of the entry.
         * @param v The value of the entry.
         */
        public XmlMapEntry(final String k, final String v){
            this.key = k != null ? k : "";
            this.value = v != null ? v : "";
        }

        /**
         * Initializes a new empty map entry.
         */
        @SuppressWarnings("UnusedDeclaration")
        public XmlMapEntry(){
            this("", "");
        }

        /**
         * Initializes a new map entry from the another entry.
         * @param source Another entry.
         */
        public XmlMapEntry(final Map.Entry<String, String> source){
            this(source.getKey(), source.getValue());
        }

        /**
         * Gets key of this entry.
         * @return The key of this entry.
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the key of this entry.
         * @param value A new key.
         */
        @SuppressWarnings("UnusedDeclaration")
        @XmlAttribute(name = "name", required = true, namespace = XmlConstants.namespace)
        public void setKey(final String value){
            this.key = value != null ? value : "";
        }

        /**
         * Returns the value of this entry.
         * @return The value of this entry.
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of this entry.
         * @param value A new value of this entry.
         */
        @SuppressWarnings("UnusedDeclaration")
        @XmlValue
        public void setValue(final String value) {
            this.value = value != null ? value : "";
        }

        /**
         * Puts this entry into the specified map.
         * @param destination The map to be modified.
         */
        public void exportTo(final Map<String, String> destination){
            destination.put(getKey(), getValue());
        }
    }

    /**
     * Represents an abstract class for configuration entity that supports a collection of custom
     * configuration parameters.
     */
    public static abstract class ConfigurationEntityWithCustomParameters implements ConfigurationEntity{
        /**
         * Represents a map of custom configuration parameters.
         */
        @XmlTransient
        protected final Map<String, String> params;

        /**
         * Initializes a new configuration entity with custom parameters.
         */
        protected ConfigurationEntityWithCustomParameters(){
            params = new HashMap<>(10);
        }

        @SuppressWarnings("UnusedDeclaration")
        @XmlElement(name = "param", namespace = XmlConstants.namespace, type = XmlMapEntry.class)
        private List<XmlMapEntry> getItems() {
            final List<XmlMapEntry> entries = new ArrayList<>(params.size());
            for(final Map.Entry<String, String> e: params.entrySet())
                entries.add(new XmlMapEntry(e));
            return entries;
        }

        @SuppressWarnings("UnusedDeclaration")
        private void setItems(final List<XmlMapEntry> items) {
            params.clear();
            for(final XmlMapEntry e: items)
                e.exportTo(params);
        }
    }

    /**
     * Represents JAXB-compliant representation of the hosting configuration.
     * This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "HostingConfiguration", namespace = XmlConstants.namespace)
    public final static class XmlHostingConfiguration extends ConfigurationEntityWithCustomParameters implements HostingConfiguration{
        @XmlTransient
        private String adapterName = "";

        /**
         * Gets the hosting adapter name.
         *
         * @return The hosting adapter name.
         */
        @Override
        public String getAdapterName() {
            return adapterName;
        }

        /**
         * Sets the hosting adapter name.
         *
         * @param value The adapter name.
         */
        @Override
        @XmlElement(nillable = false, required = true, namespace = XmlConstants.namespace, name = "adapterName")
        public void setAdapterName(final String value) {
            this.adapterName = value != null ? value : "";
        }

        /**
         * Returns a dictionary of hosting parameters, such as port and hosting address.
         *
         * @return The map of additional configuration elements.
         */
        @Override
        @XmlTransient
        public Map<String, String> getHostingParams() {
            return params;
        }
    }

    /**
     * Represents configuration of the management target. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    @XmlType(name = "ManagementTarget", namespace = XmlConstants.namespace)
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static final class XmlManagementTargetConfiguration extends ConfigurationEntityWithCustomParameters implements ManagementTargetConfiguration{
        /**
         * Represents configuration of the management attribute. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        @XmlType(name = "EventConfig", namespace = XmlConstants.namespace)
        @XmlAccessorType(XmlAccessType.PROPERTY)
        public static final class XmlEventConfiguration extends ConfigurationEntityWithCustomParameters implements EventConfiguration{
            @XmlTransient
            private String category = "";
            @XmlTransient
            private String postfix = "";

            /**
             * Gets unique postfix of this event.
             * @return Unique postfix of this event.
             */
            @XmlAttribute(name = "id", namespace = XmlConstants.namespace, required = true)
            public String getPostfix(){
                return postfix;
            }

            /**
             * Sets event postfix.
             * @param value A new event postfix.
             */
            public void setPostfix(final String value){
                this.postfix = value != null ? value : "";
            }

            /**
             * Gets the event category.
             *
             * @return The event category.
             */
            @Override
            @XmlAttribute(name = "category", namespace = XmlConstants.namespace, required = true)
            public String getCategory() {
                return category;
            }

            /**
             * Sets the category of the event to listen.
             *
             * @param value The category of the event to listen.
             */
            @Override
            public void setCategory(final String value) {
                this.category = value != null ? value : "";
            }

            /**
             * Gets a map of event options.
             *
             * @return The map of event options.
             */
            @Override
            @XmlTransient
            public Map<String, String> getAdditionalElements() {
                return params;
            }
        }

        /**
         * Represents configuration of the management attribute. This class cannot be inherited.
         * @author Roman Sakno
         * @since 1.0
         * @version 1.0
         */
        @XmlType(name = "AttributeConfig", namespace = XmlConstants.namespace)
        @XmlAccessorType(XmlAccessType.PROPERTY)
        public static final class XmlAttributeConfiguration extends ConfigurationEntityWithCustomParameters implements AttributeConfiguration{
            /**
             * Represents adapter for {@link TimeSpan} class. This class cannot be inherited.
             * @author Roman Sakno
             * @since 1.0
             * @version 1.0
             */
            public static final class TimeSpanAdapter extends XmlAdapter<Long, TimeSpan>{

                @Override
                public TimeSpan unmarshal(final Long v) throws Exception {
                    return v != null && v > 0L ? new TimeSpan(v) : TimeSpan.INFINITE;
                }

                @Override
                public Long marshal(final TimeSpan v) throws Exception {
                    return v != TimeSpan.INFINITE ? v.convert(TimeUnit.MILLISECONDS).duration : -1L;
                }
            }

            @XmlTransient
            private TimeSpan timeout = TimeSpan.INFINITE;
            @XmlTransient
            private String name = "";
            @XmlTransient
            private String postfix = "";

            /**
             * Gets unique postfix of this attribute.
             * <p>
             *     The full unique name of the attribute consists of the management target namespace
             *     and attribute postfix.
             * </p>
             * @return Unique postfix of this attribute.
             */
            @XmlAttribute(name = "id", namespace = XmlConstants.namespace, required = true)
            public String getPostfix(){
                return postfix;
            }

            /**
             * Sets unique postfix
             * @param value A new postfix for the attribute.
             */
            public void setPostfix(final String value){
                this.postfix = value != null ? value : "";
            }

            /**
             * Gets attribute value invoke/write operation timeout.
             *
             * @return Gets attribute value invoke/write operation timeout.
             */
            @Override
            @XmlElement(required = false, name = "readWriteTimeout", namespace = XmlConstants.namespace)
            @XmlJavaTypeAdapter(TimeSpanAdapter.class)
            public TimeSpan getReadWriteTimeout() {
                return timeout;
            }

            /**
             * Sets attribute value invoke/write operation timeout.
             * @param value A new timeout value.
             */
            @Override
            public void setReadWriteTimeout(final TimeSpan value) {
                timeout = value;
            }

            /**
             * Returns the attribute name.
             * @return The attribute name,
             */
            @Override
            @XmlAttribute(name = "name", namespace = XmlConstants.namespace, required = true)
            public String getAttributeName() {
                return name;
            }

            /**
             * Sets the attribute name.
             * @param value The attribute name.
             */
            @Override
            public void setAttributeName(final String value) {
                this.name = value != null ? value : "";
            }

            /**
             * Returns the additional configuration elements.
             *
             * @return The map of additional configuration elements.
             */
            @Override
            @XmlTransient
            public Map<String, String> getAdditionalElements() {
                return params;
            }
        }
        @XmlTransient
        private String connectionString = "";
        @XmlTransient
        private String connectionType = "";
        @XmlTransient
        private String namespace = "";
        @XmlTransient
        private String name = "";
        @XmlTransient
        private final Map<String, AttributeConfiguration> attributes = new HashMap<>(10);
        @XmlTransient
        private final Map<String, EventConfiguration> events = new HashMap<>(10);

        /**
         * Gets unique name of this management target.
         * @return The unique name of this management target.
         */
        @XmlAttribute(name = "name", namespace = XmlConstants.namespace)
        public String getName(){
            return name;
        }

        /**
         * Sets unique name of this management target.
         * @param value The name of the management target (short remote server name).
         */
        public void setName(final String value){
            this.name = value != null ? value : "";
        }

        /**
         * Gets the management target connection string.
         *
         * @return The connection string that is used to connect to the management server.
         */
        @Override
        @XmlElement(name = "connectionString", namespace = XmlConstants.namespace, required = true, nillable = false)
        public String getConnectionString() {
            return connectionString;
        }

        /**
         * Sets the management target connection string.
         *
         * @param value The connection string that is used to connect to the management server.
         */
        @Override
        public void setConnectionString(final String value) {
            this.connectionString = value != null ? value : "";
        }

        /**
         * Gets the type of the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @return The management connector type.
         */
        @Override
        public String getConnectionType() {
            return connectionType;
        }

        /**
         * Sets the management connector that is used to organize monitoring data exchange between
         * agent and the management provider.
         *
         * @param value The management connector type.
         */
        @Override
        @XmlElement(name = "connectionType", namespace = XmlConstants.namespace, required = true, nillable = false)
        public void setConnectionType(final String value) {
            this.connectionType = value != null ? value : "";
        }

        /**
         * Returns the monitoring namespace that is visible outside from the agent and the front-end.
         *
         * @return The namespace of the management target (such as SNMP OID prefix).
         */
        @Override
        public String getNamespace() {
            return namespace;
        }

        /**
         * Sets the monitoring namespace.
         *
         * @param value The namespace of the management target (such as SNMP OID prefix).
         */
        @Override
        @XmlElement(name = "namespace", namespace = XmlConstants.namespace, required = true, nillable = false)
        public void setNamespace(final String value) {
            this.namespace = value != null ? value : "";
        }

        /**
         * Gets a collection of configured manageable elements for this target.
         *
         * @param elementType The type of the manageable element.
         * @return A map of manageable elements; or {@literal null}, if element type is not supported.
         * @see com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration
         * @see com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration
         */
        @SuppressWarnings("unchecked")
        @Override
        public <T extends ManageableEntity> Map<String, T> getElements(final Class<T> elementType) {
            if(elementType == null) return null;
            else if(Objects.equals(elementType, AttributeConfiguration.class))
                return (Map<String, T>)getAttributes();
            else if(Objects.equals(elementType, EventConfiguration.class))
                return (Map<String, T>)getEvents();
            else return null;
        }

        /**
         * Creates a new instances of the specified manageable element.
         *
         * @param elementType Type of the required manageable element.
         * @return A new empty manageable element; or {@literal null},
         * if the specified element type is not supported.
         */
        @Override
        public <T extends ManageableEntity> T newElement(final Class<T> elementType) {
            if(elementType == null) return null;
            else if(elementType.isAssignableFrom(XmlAttributeConfiguration.class))
                return elementType.cast(newAttributeConfiguration());
            else if(elementType.isAssignableFrom(XmlEventConfiguration.class))
                return elementType.cast(newEventConfiguration());
            else return null;
        }

        @SuppressWarnings("UnusedDeclaration")
        @XmlElement(name = "attribute", namespace = XmlConstants.namespace, type = XmlAttributeConfiguration.class)
        private List<XmlAttributeConfiguration> getAttributesInternal() {
            final List<XmlAttributeConfiguration> result = new ArrayList<>(attributes.size());
            for(final String postfix: attributes.keySet()){
                final AttributeConfiguration attr = attributes.get(postfix);
                if(attr instanceof XmlAttributeConfiguration){
                    final XmlAttributeConfiguration xmlattr = (XmlAttributeConfiguration)attr;
                    xmlattr.setPostfix(postfix);
                    result.add(xmlattr);
                }
            }
            return result;
        }

        @SuppressWarnings("UnusedDeclaration")
        private void setAttributesInternal(final List<XmlAttributeConfiguration> items) {
            attributes.clear();
            for(final XmlAttributeConfiguration i: items)
                attributes.put(i.getPostfix(), i);
        }

        /**
         * Returns the management attributes (key is a attribute identifier).
         *
         * @return The dictionary of management attributes.
         */
        @XmlTransient
        public Map<String, AttributeConfiguration> getAttributes() {
            return attributes;
        }

        @SuppressWarnings("UnusedDeclaration")
        @XmlElement(name = "event", namespace = XmlConstants.namespace, type = XmlEventConfiguration.class)
        private List<XmlEventConfiguration> getEventsInternal() {
            final List<XmlEventConfiguration> result = new ArrayList<>(events.size());
            for(final String postfix: events.keySet()){
                final EventConfiguration ev = events.get(postfix);
                if(ev instanceof XmlEventConfiguration){
                    final XmlEventConfiguration xmlev = (XmlEventConfiguration)ev;
                    xmlev.setPostfix(postfix);
                    result.add(xmlev);
                }
            }
            return result;
        }

        @SuppressWarnings("UnusedDeclaration")
        private void setEventsInternal(final List<XmlEventConfiguration> items) {
            events.clear();
            for(final XmlEventConfiguration i: items)
                events.put(i.getPostfix(), i);
        }

        /**
         * Returns the event sources.
         *
         * @return A set of event sources.
         */
        @XmlTransient
        public Map<String, EventConfiguration> getEvents() {
            return events;
        }

        /**
         * Returns the dictionary of additional configuration elements.
         *
         * @return The dictionary of additional configuration elements.
         */
        @Override
        @XmlTransient
        public Map<String, String> getAdditionalElements() {
            return params;
        }

        /**
         * Creates an empty attribute configuration.
         * <p>
         * Usually, this method is used for adding new attributes in the map
         * returned by {@link #getAttributes()} method.
         * </p>
         *
         * @return An empty attribute configuration.
         */
        @SuppressWarnings("UnusedDeclaration")
        public XmlAttributeConfiguration newAttributeConfiguration() {
            return new XmlAttributeConfiguration();
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
        @SuppressWarnings("UnusedDeclaration")
        public XmlEventConfiguration newEventConfiguration() {
            return new XmlEventConfiguration();
        }
    }

    @XmlTransient
    private XmlHostingConfiguration agentConfig = new XmlHostingConfiguration();
    @XmlTransient
    private Map<String, ManagementTargetConfiguration> managementTargets = new HashMap<>(10);

    /**
     * Creates clone of this configuration.
     *
     * @return The cloned instance of this configuration.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public XmlAgentConfiguration clone() {
        final XmlAgentConfiguration newInstance = new XmlAgentConfiguration();
        newInstance.load(this);
        return newInstance;
    }

    /**
     * Returns the agent hosting configuration.
     *
     * @return The agent hosting configuration.
     */
    @Override
    public XmlHostingConfiguration getAgentHostingConfig() {
        return agentConfig;
    }

    /**
     * Sets agent hosting configuration.
     * @param value The agent hosting configuration.
     */
    @SuppressWarnings("UnusedDeclaration")
    @XmlElement(name = "hosting", namespace = XmlConstants.namespace, required = true, nillable = false)
    public void setAgentHostingConfig(final XmlHostingConfiguration value){
        this.agentConfig = value != null ? value : new XmlHostingConfiguration();
    }

    /**
     * Represents management targets.
     *
     * @return The dictionary of management targets (management back-ends).
     */
    @Override
    @XmlTransient
    public Map<String, ManagementTargetConfiguration> getTargets() {
        return managementTargets;
    }

    @SuppressWarnings("UnusedDeclaration")
    @XmlElement(name = "managementTarget", namespace = XmlConstants.namespace, type = XmlManagementTargetConfiguration.class)
    private List<XmlManagementTargetConfiguration> getTargetsInternal() {
        final List<XmlManagementTargetConfiguration> result = new ArrayList<>(managementTargets.size());
        for(final String name: managementTargets.keySet()){
            final ManagementTargetConfiguration target = managementTargets.get(name);
            if(target instanceof XmlManagementTargetConfiguration){
                final XmlManagementTargetConfiguration xmlTarget = (XmlManagementTargetConfiguration)target;
                xmlTarget.setName(name);
                result.add(xmlTarget);
            }
        }
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    private void setTargetsInternal(final List<XmlManagementTargetConfiguration> items) {
        managementTargets.clear();
        for(final XmlManagementTargetConfiguration i: items)
            managementTargets.put(i.getName(), i);
    }

    /**
     * Empty implementation of ManagementTargetConfiguration interface
     *
     * @return implementation of ManagementTargetConfiguration interface
     */
    @Override
    public XmlManagementTargetConfiguration newManagementTargetConfiguration() {
        return new XmlManagementTargetConfiguration();
    }

    /**
     * Serializes this object into the specified stream.
     *
     * @param output An output stream that receives configuration in XML format.
     * @throws UnsupportedOperationException Serialization is not supported.
     * @throws java.io.IOException           Some I/O error occurs.
     */
    @Override
    public void save(final OutputStream output) throws IOException {
        try {
            final JAXBContext jaxb = JAXBContext.newInstance(XmlAgentConfiguration.class);
            final Marshaller serializer = jaxb.createMarshaller();
            serializer.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            serializer.marshal(this, output);
        }
        catch (final JAXBException e) {
            throw new IOException(e);
        }
    }

    /**
     * Serializes this configuration into XML string.
     * @return XML representation of this instance.
     */
    @Override
    public String toString() {
        try{
            return toXmlString();
        }
        catch (final IOException e) {
            return e.toString();
        }
    }

    /**
     * Serializes this configuration into XML string and throws an exception if this is not possible.
     * @return XML representation of this instance.
     * @throws IOException
     */
    public String toXmlString() throws IOException{
        try(final ByteArrayOutputStream output = new ByteArrayOutputStream()){
            save(output);
            return output.toString("UTF-8");
        }
    }

    /**
     * Restores state of this configuration object from the XML string.
     * @param value XML configuration to load.
     * @throws IOException
     */
    @SuppressWarnings("UnusedDeclaration")
    public void fromXmlString(final String value) throws IOException{
        try(final ByteArrayInputStream input = new ByteArrayInputStream(value.getBytes("UTF-8"))){
            load(input);
        }
    }

    /**
     * Reads the file and fills the current instance.
     *
     * @param input A stream that contains configuration in XML format.
     * @throws UnsupportedOperationException Deserialization is not supported.
     * @throws java.io.IOException           Cannot invoke from the specified stream.
     */
    @Override
    public void load(final InputStream input) throws IOException {
        try {
            final JAXBContext jaxb = JAXBContext.newInstance(XmlAgentConfiguration.class);
            final Unmarshaller deserializer = jaxb.createUnmarshaller();
            load((XmlAgentConfiguration)deserializer.unmarshal(input));
        }
        catch (final JAXBException e) {
            throw new IOException(e);
        }
    }
}
