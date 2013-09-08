package com.snamp.connectors;

import com.snamp.TimeSpan;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Represents an abstract class for building custom management connectors.
 * @author roman
 */
public abstract class ManagementConnectorBase implements ManagementConnector {
    /**
     * Represents connection string.
     */
    protected String connectionString;

    /**
     * Represents connection properties.
     */
    protected final Properties connectionProperties;

    private final String[] capabilities;

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

    @Override
    public AttributeMetadata connectAttribute(String id, String namespace, String attributeName, AttributeConnectionOptions options, Set<Object> tags) {
        verifyInitialization();
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getAttribute(String id, TimeSpan readTimeout, Object defaultValue) throws TimeoutException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> getAttributes(Map<String, Object> output, TimeSpan readTimeout) throws TimeoutException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
