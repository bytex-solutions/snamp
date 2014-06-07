package com.itworks.snamp.management;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.connectors.AbstractManagedResourceActivator;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.internal.Utils;
import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.FunctorException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * Represents partial implementation of {@link com.itworks.snamp.management.SnampManager} service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractSnampManager extends AbstractAggregator implements SnampManager {
    /**
     * Represents superclass for managed resource connector descriptor.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    protected static abstract class ResourceConnectorDescriptor implements SnampComponentDescriptor {
        /**
         * Represents name of the managed resource connector.
         */
        protected final String systemName;

        protected ResourceConnectorDescriptor(final String connectorName){
            this.systemName = connectorName;
        }

        /**
         * Represents the bundle context of the derived class.
         * <p>
         *     Use {@link com.itworks.snamp.internal.Utils#getBundleContextByObject(Object)} with
         *     {@literal this} parameter to implement this method.
         * </p>
         * @return The bundle context of the derived class.
         */
        protected abstract BundleContext getItselfContext();

        /**
         * Gets state of the component.
         *
         * @return The state of the component.
         * @see org.osgi.framework.Bundle#ACTIVE
         * @see org.osgi.framework.Bundle#INSTALLED
         * @see org.osgi.framework.Bundle#UNINSTALLED
         * @see org.osgi.framework.Bundle#RESOLVED
         * @see org.osgi.framework.Bundle#STARTING
         * @see org.osgi.framework.Bundle#STOPPING
         */
        @Override
        public int getState() {
            return ManagedResourceConnectorClient.getState(getItselfContext(), systemName);
        }

        /**
         * Gets human-readable name of this component.
         *
         * @param loc Human-readable name of this component.
         * @return Human-readable name of this component.
         */
        @Override
        public String getName(final Locale loc) {
            return ManagedResourceConnectorClient.getDisplayName(getItselfContext(), systemName, loc);
        }

        /**
         * Returns the localized description of this object.
         *
         * @param locale The locale of the description. If it is {@literal null} then returns description
         *               in the default locale.
         * @return The localized description of this object.
         */
        @Override
        public String getDescription(final Locale locale) {
            return ManagedResourceConnectorClient.getDescription(getItselfContext(), systemName, locale);
        }

        /**
         * Gets version of this component.
         *
         * @return The version of this component.
         */
        @Override
        public Version getVersion() {
            return ManagedResourceConnectorClient.getVersion(getItselfContext(), systemName);
        }

        /**
         * Gets SNAMP component management service and pass it to the user-defined action.
         *
         * @param serviceType    Requested service contract.
         * @param serviceInvoker User-defined action that is used to perform some management actions.
         * @throws org.apache.commons.collections4.FunctorException An exception occurred during processing.
         * @see org.apache.commons.collections4.FunctorException#getCause()
         * @see com.itworks.snamp.management.Maintainable
         * @see com.itworks.snamp.licensing.LicensingDescriptionService
         */
        @Override
        public final  <S extends ManagementService> void invokeManagementService(final Class<S> serviceType, final Closure<S> serviceInvoker) throws FunctorException {
            ServiceReference<S> ref = null;
            try {
                ref = ManagedResourceConnectorClient.getServiceReference(getItselfContext(), systemName, null, serviceType);
                serviceInvoker.execute(getItselfContext().getService(ref));
            }
            catch (final InvalidSyntaxException e) {
                throw new FunctorException(e);
            }
            finally {
                if(ref != null) getItselfContext().ungetService(ref);
            }
        }
    }

    /**
     * Creates a new instance of the connector descriptor.
     * @param systemName The name of the connector.
     * @return A new instance of the connector descriptor.
     */
    protected abstract ResourceConnectorDescriptor createResourceConnectorDescriptor(final String systemName);

    /**
     * Returns a read-only collection of installed resource connectors.
     *
     * @return A read-only collection of installed resource connectors.
     */
    @Override
    public final Collection<SnampComponentDescriptor> getInstalledResourceConnectors() {
        final Collection<String> systemNames = AbstractManagedResourceActivator.getInstalledResourceConnectors(Utils.getBundleContextByObject(this));
        final Collection<SnampComponentDescriptor> result = new ArrayList<>(systemNames.size());
        for(final String systemName: systemNames)
            result.add(createResourceConnectorDescriptor(systemName));
        return result;
    }

    /**
     * Returns a read-only collection of installed resource adapters.
     *
     * @return A read-only collection of installed resource adapters.
     */
    @Override
    public Collection<SnampComponentDescriptor> getInstalledResourceAdapters() {
        return null;
    }

    /**
     * Returns a read-only collection of installed additional SNAMP components.
     *
     * @return A read-only collection of installed additional SNAMP components.
     */
    @Override
    public Collection<SnampComponentDescriptor> getInstalledComponents() {
        return null;
    }
}
