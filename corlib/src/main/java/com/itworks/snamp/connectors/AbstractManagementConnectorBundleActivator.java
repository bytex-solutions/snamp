package com.itworks.snamp.connectors;

import com.itworks.snamp.core.AbstractBundleActivator;

/**
 * Represents a base class for management connector bundle.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractManagementConnectorBundleActivator<TConnector extends ManagementConnector> extends AbstractBundleActivator {
    private final String connectorName;

    /**
     * Initializes a new connector factory.
     * @param connectorName The name of the connector.
     * @exception IllegalArgumentException connectorName is null.
     */
    protected AbstractManagementConnectorBundleActivator(final String connectorName){
        super(String.format("snamp.connectors.%s", connectorName));
        this.connectorName = connectorName;
    }

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
    public final boolean equals(final AbstractManagementConnectorBundleActivator<?> factory){
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
        return factory instanceof AbstractManagementConnectorBundleActivator && equals((AbstractManagementConnectorBundleActivator<?>)factory);
    }
}
