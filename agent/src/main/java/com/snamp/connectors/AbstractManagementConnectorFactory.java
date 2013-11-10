package com.snamp.connectors;

import com.snamp.AbstractPlatformService;
import net.xeoh.plugins.base.annotations.Capabilities;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a base class for building management connectors.
 * @author Roman Sakno
 */
public abstract class AbstractManagementConnectorFactory<TConnector extends ManagementConnector> extends AbstractPlatformService implements ManagementConnectorFactory {
    private final String connectorName;

    /**
     * Initializes a new connector factory.
     * @param connectorName The name of the connector.
     * @exception IllegalArgumentException connectorName is null.
     */
    protected AbstractManagementConnectorFactory(final String connectorName){
        super(getLogger(connectorName));
        if(connectorName == null) throw new IllegalArgumentException("connectorName is null.");
        this.connectorName = connectorName;
    }

    public static Logger getLogger(final String connectorName){
        return Logger.getLogger(String.format("snamp.connectors.%s.log", connectorName));
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
    public abstract TConnector newInstance(final String connectionString, final Map<String, String> connectionProperties);

    /**
     * Returns the connector name.
     * @return The connector name.
     */
    @Override
    public final String toString(){
        return connectorName;
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector; otherwise, {@literal false}.
     */
    public final boolean equals(final AbstractManagementConnectorFactory<?> factory){
        return factory != null && connectorName.equals(factory.connectorName);
    }

    /**
     * Determines whether the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector.
     * @param factory The factory to compare.
     * @return {@literal true}, if the specified factory equals to this factory and produces
     * the same type of the SNAMP management connector; otherwise, {@literal false}.
     */
    @Override
    public final boolean equals(final Object factory){
        return factory instanceof AbstractManagementConnectorFactory && equals((AbstractManagementConnectorFactory<?>)factory);
    }
}
