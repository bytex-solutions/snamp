package com.snamp.connectors;

import net.xeoh.plugins.base.annotations.Capabilities;

import java.util.Map;

/**
 * Represents a base class for building management connectors.
 * @author roman
 */
public abstract class ManagementConnectorFactoryBase<TConnector extends ManagementConnector> implements ManagementConnectorFactory {
    private final String connectorName;

    /**
     * Initializes a new connector factory.
     * @param connectorName The name of the connector.
     * @exception IllegalArgumentException connectorName is null.
     */
    protected ManagementConnectorFactoryBase(final String connectorName){
        if(connectorName == null) throw new IllegalArgumentException("connectorName is null.");
        this.connectorName = connectorName;
    }

    /**
     * Creates a new array of capabilities for JSPF infrastructure, you should not use this method directly
     * in your code.
     * @param connectorName The name of the connector.
     * @return An array of plug-in capabilities.
     */
    public static String[] makeCapabilities(final String connectorName){
        return new String[]{
            String.format("connector:%s", connectorName)
        };
    }

    /**
     * Returns an array of plug-in capabilities.
     * @return An array of plug-in capabilities.
     */
    @Capabilities
    public final String[] capabilities(){
        return makeCapabilities(connectorName);
    }

    /**
     * Creates a new instance of the connector.
     * @param connectionString The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     * @return A new instance of the management connector.
     */
    @Override
    public abstract TConnector newInstance(final String connectionString, final Map<String, Object> connectionProperties);

    /**
     * Returns the connector name.
     * @return The connector name.
     */
    @Override
    public final String toString(){
        return connectorName;
    }

    /**
     *
     * @param factory
     * @return
     */
    public final boolean equals(final ManagementConnectorFactoryBase<?> factory){
        return factory != null && connectorName.equals(factory.connectorName);
    }

    /**
     *
     * @param factory
     * @return
     */
    @Override
    public final boolean equals(final Object factory){
        return factory instanceof ManagementConnectorFactoryBase && equals((ManagementConnectorFactoryBase<?>)factory);
    }
}
