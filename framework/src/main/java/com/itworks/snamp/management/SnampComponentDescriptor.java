package com.itworks.snamp.management;

import com.itworks.snamp.Descriptive;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.FunctorException;
import org.osgi.framework.Version;

import java.util.Locale;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface SnampComponentDescriptor extends Descriptive, Map<String, String> {
    /**
     * Represents name of the property that contains connector name.
     */
    String CONNECTOR_SYSTEM_NAME_PROPERTY = "connectorName";
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
     * @return {@literal true}, if the requested service is invoked; otherwise, {@literal false}.
     * @throws FunctorException An exception occurred during processing.
     * @see org.apache.commons.collections4.FunctorException#getCause()
     * @see com.itworks.snamp.management.Maintainable
     * @see com.itworks.snamp.licensing.LicensingDescriptionService
     */
    <S extends ManagementService> boolean invokeManagementService(final Class<S> serviceType, final Closure<S> serviceInvoker) throws FunctorException;
}
