package com.bytex.snamp.management;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Localizable;
import com.bytex.snamp.core.SupportService;
import org.osgi.framework.Version;

import java.util.Locale;
import java.util.Map;

/**
 * Describes SNAMP component.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface SnampComponentDescriptor extends Localizable, Map<String, String> {
    /**
     * Represents name of the property that contains connector name.
     */
    String CONNECTOR_SYSTEM_NAME_PROPERTY = "connectorType";
    /**
     * Represents name of the property that contains adapter name.
     */
    String ADAPTER_SYSTEM_NAME_PROPERTY = "adapterName";

    /**
     * Represents name of the property that contains bundle identifier.
     */
    String BUNDLE_ID_PROPERTY = "bundleID";

    /**
     * Gets state of the component.
     * @return The state of the component.
     * @see org.osgi.framework.Bundle#ACTIVE
     * @see org.osgi.framework.Bundle#INSTALLED
     * @see org.osgi.framework.Bundle#UNINSTALLED
     * @see org.osgi.framework.Bundle#RESOLVED
     * @see org.osgi.framework.Bundle#STARTING
     * @see org.osgi.framework.Bundle#STOPPING
     */
    int getState();

    /**
     * Gets human-readable name of this component.
     * @param loc Human-readable name of this component.
     * @return Human-readable name of this component.
     */
    String getName(final Locale loc);

    /**
     * Gets version of this component.
     * @return The version of this component.
     */
    Version getVersion();


    /**
     * Gets SNAMP component management service and pass it to the user-defined action.
     * @param serviceType Requested service contract.
     * @param serviceInvoker User-defined action that is used to perform some management actions.
     * @param <S> Type of the management service contract.
     * @param <E> Type of the exception that may be raised by invoker.
     * @return {@literal true}, if the requested service is invoked; otherwise, {@literal false}.
     * @throws E An exception raised by service invoker.
     * @see com.bytex.snamp.management.Maintainable
     * @see com.bytex.snamp.connectors.discovery.DiscoveryService
     * @see com.bytex.snamp.configuration.ConfigurationEntityDescriptionProvider
     */
    <S extends SupportService, E extends Exception> boolean invokeSupportService(final Class<S> serviceType, final Acceptor<S, E> serviceInvoker) throws E;
}
