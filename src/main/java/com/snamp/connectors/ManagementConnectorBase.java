package com.snamp.connectors;

import com.snamp.TimeSpan;

import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.*;

/**
 * Represents an abstract class for building custom management connectors.
 * @author roman
 */
public abstract class ManagementConnectorBase implements ManagementConnector {

    /**
     * Represents default implementation of the attribute descriptor.
     */
    protected static class GenericAttributeMetadata implements AttributeMetadata {
        private final String attributeName;
        private final String namespace;
        private final AttributeConnectionOptions options;
        private final HashSet<Object> tags;

        public GenericAttributeMetadata(final String attributeName, final String namespace, final AttributeConnectionOptions options){
            if(attributeName == null) throw new IllegalArgumentException("attributeName is null.");
            else if(namespace == null) throw new IllegalArgumentException("namespace is null.");
            this.attributeName = attributeName;
            this.namespace = namespace;
            this.options = options;
            this.tags = new HashSet<Object>();
        }

        /**
         * Returns the attribute name.
         * @return The attribute name.
         */
        @Override
        public final String getAttributeName() {
            return attributeName;
        }

        /**
         * @return
         */
        @Override
        public final String getNamespace() {
            return namespace;
        }

        /**
         * By default, returns {@literal true}.
         * @return
         */
        @Override
        public boolean canRead() {
            return true;
        }

        /**
         * Determines whether the value of this attribute can be changed, returns {@literal true} by default.
         *
         * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
         */
        @Override
        public boolean canWrite() {
            return true;
        }

        /**
         * Returns the mutable set of tags.
         *
         * @return The mutable set of custom tags.
         */
        @Override
        public final Set<Object> tags() {
            return tags;
        }

        /**
         * Determines whether the value of the attribute can be cached after first reading
         * and supplied as real attribute value before first write, return {@literal false} by default.
         *
         * @return {@literal true}, if the value of this attribute can be cached; otherwise, {@literal false}.
         */
        @Override
        public boolean cacheable() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        /**
         * Returns the attribute connection options.
         *
         * @return The attribute connection options.
         */
        @Override
        public final AttributeConnectionOptions options() {
            return options;
        }
    }

    /**
     * Represents connection string.
     */
    protected String connectionString;

    /**
     * Represents connection properties.
     */
    protected final Properties connectionProperties;

    private final String[] capabilities;
    private final ReadWriteLock coordinator; //transaction coordinator

    /**
     * Represents a dictionary of connected attributes.
     */
    protected final Map<String, AttributeMetadata> attributes;

    /**
     * Represents connector name.
     */
    protected final String connectorName;

    static String createConnectorFeature(final String connectorName){
        return String.format("connector:%s", connectorName);
    }

    /**
     * Initializes a new management connector.
     */
    protected ManagementConnectorBase(final String connectorName, final String... capabilities){
        this.connectionProperties = new Properties();
        this.connectionString = null;
        this.capabilities = new String[capabilities.length + 1];
        this.capabilities[0] = createConnectorFeature(this.connectorName = connectorName);
        for(int i = 0; i < capabilities.length; i++) this.capabilities[i + 1] = capabilities[i];
        this.attributes = new HashMap<>();
        this.coordinator = new ReentrantReadWriteLock();
    }

    /**
     * Returns the set of capabilities for JSPF infrastructure, you should not use this method directly.
     * @return An array of capabilities.
     */
    public final String[] capabilities(){
        return this.capabilities;
    }

    /**
     *  Throws an exception if the connector is not initialized.
     */
    protected final void verifyInitialization(){
        if(this.connectionString == null)
            throw new IllegalStateException("Management connector should be initialized");
    }

    /**
     * Initialize the management connector.
     * @param connectionString Connection string.
     * @param connectionProperties Connection parameters.
     * @return {@literal true}, if this instance is initialized successfully; otherwise, {@literal false}.
     */
    protected abstract boolean initializeCore(final String connectionString, final Properties connectionProperties);

    /**
     * Initialize the management connector.
     * @param connectionString Connection string.
     * @param connectionProperties Connection parameters.
     * @return {@literal true}, if this instance is initialized successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean initialize(final String connectionString, final Properties connectionProperties) {
        final boolean result;
        //the custom method should not initialize protected fields associated with initialization.
        if(result = initializeCore(connectionString, connectionProperties)){
            this.connectionString = connectionString;
            this.connectionProperties.clear();
            this.connectionProperties.putAll(connectionProperties);
        }
        return  result;
    }

    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to read attribute from this connector.
     * @param namespace The namespace of the attribute.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @param tags The set of custom objects associated with the attribute.
     * @return The description of the attribute.
     */
    protected abstract AttributeMetadata connectAttributeCore(final String id, final String namespace, final String attributeName, final AttributeConnectionOptions options, final Set<Object> tags);

    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to read attribute from this connector.
     * @param namespace The namespace of the attribute.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @param tags The set of custom objects associated with the attribute.
     * @return The description of the attribute.
     */
    @Override
    public synchronized final AttributeMetadata connectAttribute(final String id, final String namespace, final String attributeName, final AttributeConnectionOptions options, final Set<Object> tags) {
        verifyInitialization();
        //return existed attribute without exception to increase flexibility of the API
        if(attributes.containsKey(id)) return attributes.get(id);
        final AttributeMetadata attr;
        if((attr = connectAttributeCore(id, namespace, attributeName, options, tags)) != null)
            attributes.put(id, attr);
        return attr;
    }

    protected abstract Object getAttributeCore(String id, TimeSpan readTimeout, final Object defaultValue) throws TimeoutException;

    @Override
    public final Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException{
        final Lock readLock = coordinator.readLock();
        if(readTimeout == TimeSpan.infinite) readLock.lock();
        else try {
            if(!readLock.tryLock(readTimeout.time, readTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
           return defaultValue;
        }
        //read lock is acquired, forces the custom reading operation
        try{
            return getAttributeCore(id, readTimeout, defaultValue);
        }
        finally {
            readLock.unlock();
        }
    }

    @Override
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException {
        final Lock readLock = coordinator.readLock();
        if(readTimeout == TimeSpan.infinite) readLock.lock();
        else try {
            if(!readLock.tryLock(readTimeout.time, readTimeout.unit))
                throw new TimeoutException("The connector runs read/write operation too long");
        } catch (InterruptedException e) {
            return new HashSet<>();
        }
        //accumulator for really existed attribute IDs
        final Set<String> result = new HashSet<>();
        try{
            final Object missing = new Object(); //this object represents default value for understanding
            //whether the attribute value is unavailable
            for(final String id: output.keySet()){
                final Object value = getAttributeCore(id, readTimeout, missing);
                if(value != missing) { //attribute value is available
                    result.add(id);
                    output.put(id, value);
                }
            }
        }
        finally {
            readLock.unlock();
        }
        return result;
    }

    @Override
    public boolean setAttribute(String id, TimeSpan writeTimeout, Object value) throws TimeoutException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setAttributes(Map<String, Object> values, TimeSpan writeTimeout) throws TimeoutException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean disconnectAttribute(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AttributeMetadata getAttributeInfo(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<String> iterator() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
